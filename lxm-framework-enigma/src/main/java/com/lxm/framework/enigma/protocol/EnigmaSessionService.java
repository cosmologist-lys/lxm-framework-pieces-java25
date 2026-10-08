package com.lxm.framework.enigma.protocol;

import com.lxm.framework.enigma.*;
import com.lxm.framework.enigma.crypto.EnigmaCrypto;
import com.lxm.framework.enigma.spi.EnigmaIdentity;
import com.lxm.framework.enigma.store.*;
import java.time.Clock;
import java.util.*;

public final class EnigmaSessionService {
    private final SessionStore store;
    private final EnigmaProperties properties;
    private final EnigmaCrypto crypto;
    private final Clock clock;
    public EnigmaSessionService(SessionStore store,EnigmaProperties properties,EnigmaCrypto crypto,Clock clock) {
        this.store=store;this.properties=properties;this.crypto=crypto;this.clock=clock;properties.validate();
    }
    public record Grant(int v,String sid,String kid,String serverTime,String refreshAt,String expiresAt,Map<String,String> keys) {
        @Override public String toString() { return "Grant[sid="+sid+",kid="+kid+"]"; }
    }
    private SessionState.KeyVersion newKey(long now) {
        var key=new SessionState.KeyVersion();key.kid=crypto.randomId();key.createdAt=now;
        key.expiresAt=now+properties.getKeyTtlMillis();key.acceptUntil=key.expiresAt;
        key.requestEncryption=EnigmaCrypto.encode(crypto.randomBytes(32));key.responseEncryption=EnigmaCrypto.encode(crypto.randomBytes(32));
        key.requestSigning=EnigmaCrypto.encode(crypto.randomBytes(32));key.responseSigning=EnigmaCrypto.encode(crypto.randomBytes(32));return key;
    }
    private Grant grant(String sid,SessionState.KeyVersion key) {
        return new Grant(1,sid,key.kid,Long.toString(clock.millis()),Long.toString(key.createdAt+properties.getRefreshAfterMillis()),Long.toString(key.expiresAt),
            Map.of("requestEncryption",key.requestEncryption,"responseEncryption",key.responseEncryption,"requestSigning",key.requestSigning,"responseSigning",key.responseSigning));
    }
    public Grant issue(EnigmaIdentity identity) {
        var state=new SessionState();state.sid=crypto.randomId();state.owner=Map.copyOf(identity.binding());
        var key=newKey(clock.millis());state.currentKid=key.kid;state.keys.put(key.kid,key);
        state.retainUntil=key.expiresAt+properties.getResponseGraceMillis();store.create(state);return grant(state.sid,key);
    }
    private SessionState.KeyVersion active(SessionState state,String kid,EnigmaIdentity identity,long now) {
        if (!state.owner.equals(identity.binding()) || state.revoked) throw EnigmaException.identity();
        var key=state.keys.get(kid);if (key==null) throw EnigmaException.key();
        if (now>=key.acceptUntil) throw EnigmaException.expired();return key;
    }
    public KeySnapshot lookup(RequestContext request,EnigmaIdentity identity) {
        long now=clock.millis(),timestamp=Long.parseLong(request.timestamp());
        if (timestamp<now-properties.getClockSkewMillis() || timestamp>now+properties.getClockSkewMillis()) throw EnigmaException.expired();
        return store.transact(request.sid(),state->KeySnapshot.of(active(state,request.kid(),identity,now)));
    }
    public Grant refresh(String sid,String currentKid,EnigmaIdentity identity) {
        return store.transact(sid,state->{
            long now=clock.millis();var old=active(state,currentKid,identity,now);
            if (old.refreshedTo!=null) {
                if (now>old.retryUntil) throw EnigmaException.expired();
                var next=state.keys.get(old.refreshedTo);if (next==null || now>=next.acceptUntil) throw EnigmaException.expired();return grant(sid,next);
            }
            if (!currentKid.equals(state.currentKid)) throw EnigmaException.key();
            state.keys.values().removeIf(key->now>=key.expiresAt+properties.getResponseGraceMillis());
            if (state.keys.size()>=16) throw EnigmaException.capacity();
            var next=newKey(now);old.refreshedTo=next.kid;old.retryUntil=Math.min(old.expiresAt,now+properties.getOverlapMillis());
            old.acceptUntil=old.retryUntil;state.keys.put(next.kid,next);state.currentKid=next.kid;
            state.retainUntil=next.expiresAt+properties.getResponseGraceMillis();return grant(sid,next);
        });
    }
    public void revoke(String sid,EnigmaIdentity identity) {
        store.transact(sid,true,state->{if (!state.owner.equals(identity.binding())) throw EnigmaException.identity();state.revoked=true;return null;});
    }
    /** 鉴权撤销事件先使登录凭证失效，再调用本方法；loginSession 必须每次重新登录都不同。 */
    public void revokeIdentity(EnigmaIdentity identity) {
        store.revokeOwner(identity.binding(),clock.millis()+properties.getKeyTtlMillis()+properties.getResponseGraceMillis());
    }
    public void admit(ProtectedContext context,String iv) {
        if (!context.authenticated) throw EnigmaException.integrity();
        store.transact(context.request.sid(),state->{
            long now=clock.millis();var key=active(state,context.request.kid(),context.identity,now);
            long timestamp=Long.parseLong(context.request.timestamp());
            if (timestamp<now-properties.getClockSkewMillis() || timestamp>now+properties.getClockSkewMillis()) throw EnigmaException.expired();
            key.nonces.entrySet().removeIf(entry->entry.getValue()<=now);
            if (key.nonces.containsKey(context.request.nonce()) || (iv!=null && key.requestIvs.contains(iv))) throw EnigmaException.replay();
            if (key.requestUses>=properties.getMaxUsesPerKey() || key.nonces.size()>=properties.getMaxNoncesPerKey()) throw EnigmaException.capacity();
            key.nonces.put(context.request.nonce(),now+properties.getNonceTtlMillis());
            if (iv!=null) key.requestIvs.add(iv);
            key.requestUses++;return null;
        });
        context.admitted=true;
    }
    public void reserveResponseIv(ProtectedContext context,String iv) {
        store.transact(context.request.sid(),true,state->{
            var key=state.keys.get(context.request.kid());
            if (key==null || clock.millis()>=key.expiresAt+properties.getResponseGraceMillis()) throw EnigmaException.expired();
            if (key.responseIvs.contains(iv)) throw EnigmaException.replay();
            if (key.responseUses>=properties.getMaxUsesPerKey()) throw EnigmaException.capacity();
            key.responseIvs.add(iv);key.responseUses++;return null;
        });
    }
}
