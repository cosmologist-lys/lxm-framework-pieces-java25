package com.lxm.framework.enigma.mvc;

import com.lxm.framework.enigma.*;
import com.lxm.framework.enigma.protocol.EnigmaSessionService;
import com.lxm.framework.enigma.protocol.RequestContext;
import com.lxm.framework.enigma.spi.*;
import com.lxm.framework.web.jsonresult.JsonResult;
import jakarta.servlet.http.*;
import org.springframework.web.bind.annotation.*;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

@RestController
@RequestMapping("/enigma/session")
public final class EnigmaSessionController {
    private final EnigmaSessionService sessions;
    private final EnigmaIdentityResolver identities;
    private final EnigmaProperties properties;

    public EnigmaSessionController(
            EnigmaSessionService sessions,
            EnigmaIdentityResolver identities,
            EnigmaProperties properties) {
        this.sessions = sessions;
        this.identities = identities;
        this.properties = properties;
    }

    private EnigmaIdentity authenticated(HttpServletRequest request, HttpServletResponse response) {
        response.setHeader("Cache-Control", "no-store");
        EnigmaTransport.validate(request, properties);
        var identity = identities.resolve(request);
        if (identity == null) throw EnigmaException.identity();
        if (identity.cookieAuthentication()) {
            if (identity.csrfToken() == null
                    || identity.csrfToken().isBlank()
                    || !MessageDigest.isEqual(
                            identity.csrfToken().getBytes(StandardCharsets.UTF_8),
                            RequestContext.header(request, "X-CSRF-Token")
                                    .getBytes(StandardCharsets.UTF_8)))
                throw EnigmaException.identity();
        }
        return identity;
    }

    @PostMapping
    public EnigmaSessionService.Grant issue(
            HttpServletRequest request, HttpServletResponse response) {
        return sessions.issue(authenticated(request, response));
    }

    public record Refresh(String sid, String currentKid) {}

    @PostMapping("/refresh")
    public EnigmaSessionService.Grant refresh(
            @RequestBody Refresh body, HttpServletRequest request, HttpServletResponse response) {
        return sessions.refresh(body.sid(), body.currentKid(), authenticated(request, response));
    }

    public record Revoke(String sid) {}

    @PostMapping("/revoke")
    public JsonResult<?> revoke(
            @RequestBody Revoke body, HttpServletRequest request, HttpServletResponse response) {
        sessions.revoke(body.sid(), authenticated(request, response));
        return JsonResult.json();
    }
}
