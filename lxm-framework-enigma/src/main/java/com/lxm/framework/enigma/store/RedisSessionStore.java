package com.lxm.framework.enigma.store;

import com.lxm.framework.enigma.*;
import com.lxm.framework.enigma.crypto.EnigmaCrypto;
import com.lxm.framework.enigma.protocol.ProtocolJson;
import redis.clients.jedis.JedisPooled;
import redis.clients.jedis.exceptions.JedisException;
import java.net.URI;
import java.time.Clock;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.util.*;
import java.util.function.Function;

/** 单 Redis 主节点的 CAS 存储。每个会话的生命周期、nonce、IV、使用量在同一记录中原子更新。 */
public final class RedisSessionStore implements SessionStore,AutoCloseable {
    private static final String CREATE="if redis.call('exists',KEYS[2])==1 then return -1 end if redis.call('exists',KEYS[1])==1 then return 0 end redis.call('hset',KEYS[1],'version','1','sealed',ARGV[1]) redis.call('pexpireat',KEYS[1],ARGV[2]) return 1";
    private static final String CAS="if ARGV[5]=='deny' and redis.call('exists',KEYS[2])==1 then return -1 end if redis.call('hget',KEYS[1],'version')~=ARGV[1] then return 0 end redis.call('hset',KEYS[1],'version',ARGV[2],'sealed',ARGV[3]) redis.call('pexpireat',KEYS[1],ARGV[4]) return 1";
    private static final String RATE="local n=redis.call('incr',KEYS[1]); if n==1 then redis.call('pexpire',KEYS[1],120000) end return n";
    private final JedisPooled jedis;
    private final EnigmaProperties properties;
    private final EnigmaCrypto crypto;
    private final Clock clock;
    private final Map<String,byte[]> wrappingKeys=new HashMap<>();
    public RedisSessionStore(EnigmaProperties properties,EnigmaCrypto crypto,Clock clock) {
        this.properties=properties;this.crypto=crypto;this.clock=clock;
        if (properties.getRedisNamespace()==null || properties.getRedisNamespace().isBlank()) throw new IllegalArgumentException("Enigma Redis namespace required");
        properties.getWrappingKeys().forEach((kid,key)->wrappingKeys.put(kid,EnigmaCrypto.decode(key,32)));
        if (!wrappingKeys.containsKey(properties.getActiveWrappingKid())) throw new IllegalArgumentException("Active key wrapping version required");
        this.jedis=new JedisPooled(URI.create(properties.getRedisUri()));
    }
    private String key(String sid) {
        if (sid==null || !sid.matches("[A-Za-z0-9_-]{22}")) throw EnigmaException.protocol();
        return properties.getRedisNamespace()+"{"+sid+"}";
    }
    private byte[] aad(String sid,String revision,String wrappingKid) { return ProtocolJson.canonical(Map.of("sid",sid,"revision",revision,"wrappingKid",wrappingKid)); }
    private byte[] wrappingInfo(String sid,String revision,String kid) {
        return ProtocolJson.canonical(Map.of("purpose","LFP-ENIGMA-WRAP-V1","sid",sid,"revision",revision,"wrappingKid",kid));
    }
    private String seal(SessionState state,String revision) {
        String kid=properties.getActiveWrappingKid();byte[] iv=crypto.randomBytes(12),salt=crypto.randomBytes(32);
        return ProtocolJson.MAPPER.writeValueAsString(Map.of("kid",kid,"salt",EnigmaCrypto.encode(salt),"iv",EnigmaCrypto.encode(iv),"ciphertext",EnigmaCrypto.encode(
            crypto.encrypt(crypto.hkdf(wrappingKeys.get(kid),salt,wrappingInfo(state.sid,revision,kid)),iv,aad(state.sid,revision,kid),ProtocolJson.MAPPER.writeValueAsBytes(state)))));
    }
    private SessionState open(String sid,String revision,String sealed) {
        try {
            var envelope=ProtocolJson.MAPPER.readTree(sealed);String kid=envelope.path("kid").asString();byte[] wrapping=wrappingKeys.get(kid);
            if (wrapping==null) throw EnigmaException.storage();
            byte[] plain=crypto.decrypt(crypto.hkdf(wrapping,EnigmaCrypto.decode(envelope.path("salt").asString(),32),wrappingInfo(sid,revision,kid)),EnigmaCrypto.decode(envelope.path("iv").asString(),12),aad(sid,revision,kid),EnigmaCrypto.decode(envelope.path("ciphertext").asString(),-1));
            var state=ProtocolJson.MAPPER.readValue(plain,SessionState.class);
            if (!sid.equals(state.sid)) throw EnigmaException.storage();
            return state;
        } catch (tools.jackson.core.JacksonException | EnigmaException invalid) { throw EnigmaException.storage(); }
    }
    public void create(SessionState state) {
        try {
            String owner=ownerHash(state.owner);
            var count=(Long)jedis.eval(RATE,List.of(properties.getRedisNamespace()+"rate:"+owner+":"+clock.millis()/60_000),List.of());
            if (count>properties.getSessionsPerMinute()) throw EnigmaException.capacity();
            long result=(Long)jedis.eval(CREATE,List.of(key(state.sid),ownerKey(state.owner)),List.of(seal(state,"1"),Long.toString(state.retainUntil)));
            if (result==-1) throw EnigmaException.identity();
            if (result!=1) throw EnigmaException.capacity();
        } catch (JedisException unavailable) { throw EnigmaException.storage(); }
    }
    public <T> T transact(String sid,boolean allowRevokedOwner,Function<SessionState,T> action) {
        try {
            for (int attempt=0;attempt<32;attempt++) {
                var record=jedis.hgetAll(key(sid));
                if (!record.containsKey("version") || !record.containsKey("sealed")) throw EnigmaException.key();
                String revision=record.get("version");var state=open(sid,revision,record.get("sealed"));
                if (state.retainUntil<=clock.millis()) throw EnigmaException.key();
                T result=action.apply(state);String next=Long.toString(Math.addExact(Long.parseLong(revision),1));
                long changed=(Long)jedis.eval(CAS,List.of(key(sid),ownerKey(state.owner)),List.of(revision,next,seal(state,next),Long.toString(state.retainUntil),allowRevokedOwner?"allow":"deny"));
                if (changed==-1) throw EnigmaException.identity();
                if (changed==1) return result;
            }
            throw EnigmaException.storage();
        } catch (JedisException | NumberFormatException unavailable) { throw EnigmaException.storage(); }
    }
    private String ownerHash(Map<String,String> owner) {
        try { return java.util.HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(ProtocolJson.canonical(owner))); }
        catch (NoSuchAlgorithmException unavailable) { throw new IllegalStateException(unavailable); }
    }
    private String ownerKey(Map<String,String> owner) { return properties.getRedisNamespace()+"revoked:"+ownerHash(owner); }
    public void revokeOwner(Map<String,String> owner,long retainUntil) {
        try { jedis.set(ownerKey(owner),"1",redis.clients.jedis.params.SetParams.setParams().px(Math.max(1,retainUntil-clock.millis()))); }
        catch (JedisException unavailable) { throw EnigmaException.storage(); }
    }
    public void close() { jedis.close(); }
}
