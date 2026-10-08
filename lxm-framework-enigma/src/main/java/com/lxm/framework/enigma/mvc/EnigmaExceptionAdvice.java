package com.lxm.framework.enigma.mvc;

import com.lxm.framework.enigma.EnigmaException;
import com.lxm.framework.web.jsonresult.JsonResult;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.bind.annotation.*;
import org.springframework.core.annotation.Order;

@RestControllerAdvice
@Order(-10000)
public final class EnigmaExceptionAdvice {
    @ExceptionHandler(EnigmaException.class)
    public JsonResult<?> handle(EnigmaException failure, HttpServletResponse response) {
        response.setStatus(failure.status());
        response.setHeader("Cache-Control", "no-store");
        return JsonResult.json(failure.code(), failure.getMessage());
    }
}
