package com.lxm.framework.auth;

import com.lxm.framework.auth.filter.LxmServletFilter;
import com.lxm.framework.auth.model.AuthFilterRegister;
import com.lxm.framework.auth.filter.ServletErrorStrategy;
import com.lxm.framework.auth.filter.ServletFilterStrategy;
import com.lxm.framework.auth.model.defaults.TokenDaoCacheDefault;
import org.springframework.mock.web.*;
import org.springframework.web.context.request.*;
import java.util.List;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class AuthRegressionTest {
    @Test void readsUnmodifiedJava17SessionFixtureWithJackson3() throws Exception {
        try(var input=getClass().getResourceAsStream("/legacy-auth/session.json")) {
            assertNotNull(input);
            var session=tools.jackson.databind.json.JsonMapper.builder().build().readValue(input,com.lxm.framework.auth.model.LxmSession.class);
            assertEquals("lfp:legacy:fixture",session.getLoginId());
            assertNotNull(session.getTokenBox("fixture-token"));
            assertEquals(7,session.getAttribute("tenant"));
        }
    }
    @Test void filterWritesJsonAndDoesNotExecuteChain() throws Exception {
        var filter=new LxmServletFilter(new AuthFilterRegister() {
            public List<String> includes() { return List.of("/**"); }
            public List<String> excludes() { return List.of(); }
            public ServletFilterStrategy whenBlocked() { return () -> { throw new IllegalArgumentException("invalid"); }; }
            public ServletErrorStrategy whenError() { return failure -> "{\"code\":1001,\"message\":\"请登录\",\"data\":null}"; }
        });
        var request=new MockHttpServletRequest("GET","/outside/api");
        var response=new MockHttpServletResponse();
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request,response));
        try {
            filter.doFilter(request,response,(req,res)->fail("Rejected authentication executed chain"));
            assertEquals(500,response.getStatus());
            assertEquals(1001,tools.jackson.databind.json.JsonMapper.builder().build().readTree(response.getContentAsString()).path("code").asInt());
        } finally { RequestContextHolder.resetRequestAttributes(); }
    }
    @Test void tokenDaoTtlUsesSecondsAndDoesNotRenewDeletedToken() {
        try (var dao=new TokenDaoCacheDefault()) {
            dao.set("token","login",10);
            assertTrue(dao.getTimeout("token")>0 && dao.getTimeout("token")<=10);
            dao.delete("token");dao.update("token","login",10);
            assertNull(dao.get("token")); assertEquals(-1,dao.getTimeout("token"));
        }
    }
}
