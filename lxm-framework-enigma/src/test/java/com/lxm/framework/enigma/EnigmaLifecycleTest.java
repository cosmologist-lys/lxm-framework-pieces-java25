package com.lxm.framework.enigma;

import com.lxm.framework.enigma.crypto.EnigmaCrypto;
import com.lxm.framework.enigma.protocol.*;
import com.lxm.framework.enigma.spi.EnigmaIdentity;
import com.lxm.framework.enigma.store.MemorySessionStore;
import org.junit.jupiter.api.Test;
import java.time.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;
import static org.junit.jupiter.api.Assertions.*;

class EnigmaLifecycleTest {
    static final class Time extends Clock {
        final AtomicLong value=new AtomicLong(1_800_000_000_000L);
        public ZoneId getZone() { return ZoneOffset.UTC; }
        public Clock withZone(ZoneId zone) { return this; }
        public Instant instant() { return Instant.ofEpochMilli(value.get()); }
    }
    final Time clock=new Time();final EnigmaCrypto crypto=new EnigmaCrypto();final EnigmaProperties properties=new EnigmaProperties();
    final EnigmaIdentity identity=new EnigmaIdentity("42","7","login-1",false,null);
    final MemorySessionStore store=new MemorySessionStore(properties,clock);
    final EnigmaSessionService sessions=new EnigmaSessionService(store,properties,crypto,clock);
    final EnigmaProtocol protocol=new EnigmaProtocol(crypto,sessions,properties,clock);
    RequestContext request(EnigmaSessionService.Grant grant,String nonce,String mode) {
        return new RequestContext(1,mode,"request",grant.sid(),grant.kid(),mode.equals("SIGN")?"GET":"POST","/api/42",
            Map.of("query",List.of("中文","+","")),mode.equals("SIGN")?"":"application/json",Long.toString(clock.millis()),nonce);
    }
    ProtectedContext context(RequestContext request) { return new ProtectedContext(request,sessions.lookup(request,identity),identity); }
    String signature(ProtectedContext context) { return EnigmaCrypto.encode(crypto.sign(EnigmaCrypto.decode(context.key.requestSigning(),32),ProtocolJson.canonical(context.request))); }
    byte[] envelope(ProtectedContext context,String iv,byte[] plaintext) {
        return ProtocolJson.MAPPER.writeValueAsBytes(Map.of("v",1,"kid",context.request.kid(),"iv",iv,"ciphertext",EnigmaCrypto.encode(crypto.encrypt(
            EnigmaCrypto.decode(context.key.requestEncryption(),32),EnigmaCrypto.decode(iv,12),ProtocolJson.canonical(context.request),plaintext))));
    }
    @Test void signIsBoundToEntireContextAndNonceIsAtomic() throws Exception {
        var grant=sessions.issue(identity);var request=request(grant,crypto.randomId(),"SIGN");var signed=context(request);String signature=signature(signed);
        var changed=new RequestContext(1,"SIGN","request",request.sid(),request.kid(),"DELETE",request.path(),request.params(),request.contentType(),request.timestamp(),request.nonce());
        assertEquals(6203,assertThrows(EnigmaException.class,()->protocol.verifySign(context(changed),signature)).code());
        var start=new CountDownLatch(1);
        try (var executor=Executors.newFixedThreadPool(2)) {
            var futures=new ArrayList<Future<Boolean>>();
            for (int i=0;i<2;i++) futures.add(executor.submit(()->{var contender=context(request);start.await();try {protocol.verifySign(contender,signature);return true;}catch(EnigmaException e){assertEquals(6204,e.code());return false;}}));
            start.countDown();int winners=0;for (var future:futures) if (future.get()) winners++;assertEquals(1,winners);
        }
    }
    @Test void ivIsRetainedAfterShortReplayWindowAndInvalidDtoCannotReleaseNonce() {
        var grant=sessions.issue(identity);String iv=EnigmaCrypto.encode(crypto.randomBytes(12));
        var first=context(request(grant,crypto.randomId(),"ENCRYPT"));byte[] body="{\"name\":\"中文\"}".getBytes(java.nio.charset.StandardCharsets.UTF_8);
        assertArrayEquals(body,protocol.decrypt(first,envelope(first,iv,body)));
        clock.value.addAndGet(properties.getNonceTtlMillis()+1);
        var second=context(request(grant,crypto.randomId(),"ENCRYPT"));
        assertEquals(6204,assertThrows(EnigmaException.class,()->protocol.decrypt(second,envelope(second,iv,body))).code());
        var malformed=context(request(grant,crypto.randomId(),"ENCRYPT"));String otherIv=EnigmaCrypto.encode(crypto.randomBytes(12));
        byte[] invalid="{\"name\":1,\"name\":2}".getBytes(java.nio.charset.StandardCharsets.UTF_8);
        assertThrows(EnigmaException.class,()->protocol.decrypt(malformed,envelope(malformed,otherIv,invalid)));
        var retry=context(malformed.request);
        assertEquals(6204,assertThrows(EnigmaException.class,()->protocol.decrypt(retry,envelope(retry,otherIv,invalid))).code());
    }
    @Test void refreshIsIdempotentAndSnapshotCanProtectRetiredResponse() {
        var grant=sessions.issue(identity);var context=context(request(grant,crypto.randomId(),"SIGN"));protocol.verifySign(context,signature(context));
        var refreshed=sessions.refresh(grant.sid(),grant.kid(),identity);var retry=sessions.refresh(grant.sid(),grant.kid(),identity);
        assertEquals(refreshed.kid(),retry.kid());assertEquals(refreshed.keys(),retry.keys());assertNotEquals(grant.keys(),refreshed.keys());
        clock.value.addAndGet(properties.getOverlapMillis()+1);
        assertEquals(6202,assertThrows(EnigmaException.class,()->sessions.lookup(request(grant,crypto.randomId(),"SIGN"),identity)).code());
        var response=protocol.protect(context,200,"{\"code\":0,\"message\":\"ok\",\"data\":null}".getBytes(java.nio.charset.StandardCharsets.UTF_8));
        assertEquals(grant.kid(),response.headers().get("X-Enigma-Kid"));
    }
    @Test void identityRevocationRejectsEverySidAndNewIssuance() {
        var first=sessions.issue(identity);var second=sessions.issue(identity);sessions.revokeIdentity(identity);
        assertEquals(6206,assertThrows(EnigmaException.class,()->sessions.lookup(request(first,crypto.randomId(),"SIGN"),identity)).code());
        assertEquals(6206,assertThrows(EnigmaException.class,()->sessions.lookup(request(second,crypto.randomId(),"SIGN"),identity)).code());
        assertEquals(6206,assertThrows(EnigmaException.class,()->sessions.issue(identity)).code());
        assertNotNull(sessions.issue(new EnigmaIdentity("42","7","new-login",false,null)));
    }
    @Test void capacityFailsClosedInsteadOfEvictingReplayEntries() {
        properties.setMaxUsesPerKey(1);var grant=sessions.issue(identity);var first=context(request(grant,crypto.randomId(),"SIGN"));protocol.verifySign(first,signature(first));
        var second=context(request(grant,crypto.randomId(),"SIGN"));assertEquals(6205,assertThrows(EnigmaException.class,()->protocol.verifySign(second,signature(second))).code());
        var replay=context(first.request);assertEquals(6204,assertThrows(EnigmaException.class,()->protocol.verifySign(replay,signature(replay))).code());
    }
    @Test void strictJsonAndQueryDecodingAgreeWithBrowserRules() {
        assertEquals(Map.of("q",List.of("a b","+",""),"bare",List.of("")),RequestContext.query("q=a+b&q=%2B&q=&bare"));
        for (String json:List.of("{\"x\":1,\"x\":2}","{} {}","{\"x\":9007199254740992}","{\"x\":\"\\ud800\"}")) {
            assertThrows(EnigmaException.class,()->ProtocolJson.read(json.getBytes(java.nio.charset.StandardCharsets.UTF_8)));
        }
        assertThrows(EnigmaException.class,()->RequestContext.query("q=%C0%AF"));
        assertThrows(EnigmaException.class,()->EnigmaCrypto.decode("AA==",1));
        assertEquals("{\"10\":1,\"2\":2}",ProtocolJson.utf8(ProtocolJson.canonical(Map.of("2",2,"10",1))));
    }
}
