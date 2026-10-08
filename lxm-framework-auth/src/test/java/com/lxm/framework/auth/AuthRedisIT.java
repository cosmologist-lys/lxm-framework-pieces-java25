package com.lxm.framework.auth;
import com.lxm.framework.auth.model.*;
import com.lxm.framework.auth.model.defaults.TokenDaoRedisDefault;
import com.lxm.framework.auth.redis.AuthRedisProperties;
import org.junit.jupiter.api.Test;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;
class AuthRedisIT {
 @Test void sessionsRoundTripAndDeletedRecordsAreNeverResurrected() {
  var properties=new AuthRedisProperties();properties.setHost("127.0.0.1");properties.setPort(Integer.getInteger("lfp.redis.port",16379));
  String id="lfp:test:auth:"+UUID.randomUUID();
  try(var first=new TokenDaoRedisDefault(properties);var second=new TokenDaoRedisDefault(properties)) {
   var session=new LxmSession(id);session.getDataMap().put("tenant",7);session.addTokenBox(new TokenBox("fixture-token","browser"));first.setSession(session,60);
   assertNotNull(second.getSession(id).getTokenBox("fixture-token"));assertEquals(7,second.getSession(id).getAttribute("tenant"));assertTrue(first.getTimeout(id)>0);
   second.delete(id);first.updateSession(session,60);assertNull(first.getSession(id));first.updateTimeout(id,60);assertEquals(-1,first.getTimeout(id));
   first.set(id,"value",0);assertEquals(0,first.getTimeout(id));second.delete(id);first.update(id,"stale",60);assertNull(second.get(id));
  }
 }
}
