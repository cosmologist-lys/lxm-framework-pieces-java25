package com.lxm.framework.enigma;

import com.lxm.framework.enigma.crypto.EnigmaCrypto;
import com.lxm.framework.enigma.protocol.*;
import com.lxm.framework.enigma.spi.EnigmaIdentity;
import com.lxm.framework.enigma.store.RedisSessionStore;
import redis.clients.jedis.JedisPooled;
import org.junit.jupiter.api.Test;
import java.time.Clock;
import java.util.*;
import java.util.concurrent.*;
import static org.junit.jupiter.api.Assertions.*;

class EnigmaRedisIT {
    @Test void twoInstancesAdmitOnlyOnceAndStoreAuthenticatedCiphertext() throws Exception {
        var properties=new EnigmaProperties();properties.setRedisUri("redis://127.0.0.1:"+Integer.getInteger("lfp.redis.port",16379)+"/0");
        properties.setRedisNamespace("lfp:enigma:test:"+UUID.randomUUID()+":");properties.setActiveWrappingKid("deployment-v1");
        var crypto=new EnigmaCrypto();properties.setWrappingKeys(Map.of("deployment-v1",EnigmaCrypto.encode(crypto.randomBytes(32))));var clock=Clock.systemUTC();
        try (var first=new RedisSessionStore(properties,crypto,clock);var second=new RedisSessionStore(properties,crypto,clock);var inspect=new JedisPooled(java.net.URI.create(properties.getRedisUri()))) {
            var one=new EnigmaSessionService(first,properties,crypto,clock);var two=new EnigmaSessionService(second,properties,crypto,clock);
            var identity=new EnigmaIdentity("42","7","redis-login",false,null);var grant=one.issue(identity);
            var request=new RequestContext(1,"SIGN","request",grant.sid(),grant.kid(),"PUT","/api",Map.of("q",List.of("中文","")),"",Long.toString(clock.millis()),crypto.randomId());
            String signature=EnigmaCrypto.encode(crypto.sign(EnigmaCrypto.decode(grant.keys().get("requestSigning"),32),ProtocolJson.canonical(request)));
            var start=new CountDownLatch(1);
            try (var executor=Executors.newFixedThreadPool(2)) {
                var results=new ArrayList<Future<Boolean>>();
                for (var service:List.of(one,two)) results.add(executor.submit(()->{var context=new ProtectedContext(request,service.lookup(request,identity),identity);start.await();try {new EnigmaProtocol(crypto,service,properties,clock).verifySign(context,signature);return true;}catch(EnigmaException error){assertEquals(6204,error.code());return false;}}));
                start.countDown();int winners=0;for(var result:results)if(result.get())winners++;assertEquals(1,winners);
            }
            String key=properties.getRedisNamespace()+"{"+grant.sid()+"}";var state=inspect.hgetAll(key);
            for(String material:grant.keys().values()) assertFalse(state.get("sealed").contains(material));
            var refreshed=one.refresh(grant.sid(),grant.kid(),identity);assertEquals(refreshed.kid(),two.refresh(grant.sid(),grant.kid(),identity).kid());
            one.revokeIdentity(identity);
            assertEquals(6206,assertThrows(EnigmaException.class,()->two.refresh(grant.sid(),refreshed.kid(),identity)).code());
            assertEquals(6206,assertThrows(EnigmaException.class,()->two.issue(identity)).code());
            // sealed 与 revision 一起绑定；篡改版本不能解释为可信状态。
            inspect.hset(key,"version","999999");
            assertEquals(6207,assertThrows(EnigmaException.class,()->second.transact(grant.sid(),true,record->null)).code());
        }
    }
}
