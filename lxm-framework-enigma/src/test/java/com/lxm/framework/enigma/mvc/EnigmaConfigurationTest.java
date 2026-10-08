package com.lxm.framework.enigma.mvc;
import com.lxm.framework.enigma.*;
import com.lxm.framework.enigma.spi.*;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.WebApplicationContextRunner;
import org.springframework.context.annotation.*;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.bind.annotation.*;
import org.springframework.mock.web.MockHttpServletRequest;
import static org.junit.jupiter.api.Assertions.*;
class EnigmaConfigurationTest {
 @Configuration(proxyBeanMethods=false) @EnableWebMvc static class Mvc {}
 @RestController static class Unsupported {
  @GetMapping("/unsupported") @EnigmaProtected public String result(){return "unsafe";}
 }
 WebApplicationContextRunner runner(){return new WebApplicationContextRunner().withUserConfiguration(Mvc.class).withConfiguration(AutoConfigurations.of(EnigmaAutoConfiguration.class)).withPropertyValues("lfp.enigma.enabled=true","lfp.enigma.store=memory");}
 @Test void missingIdentityAndStoreFailStartupAndUnsupportedEndpointIsRejected() {
  runner().run(context->assertNotNull(context.getStartupFailure()));
  runner().withBean(EnigmaIdentityResolver.class,()->request->null).withPropertyValues("lfp.enigma.store=unspecified").run(context->assertNotNull(context.getStartupFailure()));
  runner().withBean(EnigmaIdentityResolver.class,()->request->null).withUserConfiguration(Unsupported.class).run(context->assertNotNull(context.getStartupFailure()));
  runner().withBean(EnigmaIdentityResolver.class,()->request->null).run(context->assertNull(context.getStartupFailure()));
 }
 @Test void transportRequiresTlsAndExactOriginsEvenForProtectedRequests() {
  var properties=new EnigmaProperties();var request=new MockHttpServletRequest();request.setRemoteAddr("127.0.0.1");request.setServerName("localhost");
  assertThrows(EnigmaException.class,()->EnigmaTransport.validate(request,properties));request.setSecure(true);request.setScheme("https");request.setServerPort(443);EnigmaTransport.validate(request,properties);
  request.addHeader("Origin","https://hostile.example");assertThrows(EnigmaException.class,()->EnigmaTransport.validate(request,properties));
  var clock=java.time.Clock.systemUTC();var crypto=new com.lxm.framework.enigma.crypto.EnigmaCrypto();
  var sessions=new com.lxm.framework.enigma.protocol.EnigmaSessionService(new com.lxm.framework.enigma.store.MemorySessionStore(properties,clock),properties,crypto,clock);
  var controller=new EnigmaSessionController(sessions,req->new EnigmaIdentity("user","tenant","login",true,"csrf-fixture"),properties);
  var cookieRequest=new MockHttpServletRequest("POST","/enigma/session");cookieRequest.setSecure(true);
  assertThrows(EnigmaException.class,()->controller.issue(cookieRequest,new org.springframework.mock.web.MockHttpServletResponse()));
  cookieRequest.addHeader("X-CSRF-Token","csrf-fixture");assertEquals(1,controller.issue(cookieRequest,new org.springframework.mock.web.MockHttpServletResponse()).v());
 }
}
