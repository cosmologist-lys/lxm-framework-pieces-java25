package org.example;

import com.lxm.framework.enigma.mvc.EnigmaProtected;
import com.lxm.framework.enigma.spi.*;
import com.lxm.framework.web.jsonresult.JsonResult;
import com.lxm.framework.web.jsonresult.annotation.JsonResultFilter;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.*;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

@Configuration(proxyBeanMethods = false)
@EnableAutoConfiguration
@Import(EnigmaDemoApplication.SampleController.class)
public class EnigmaDemoApplication {
    public static final AtomicInteger calls = new AtomicInteger();

    @Bean
    EnigmaIdentityResolver identityResolver() {
        // 仅测试夹具：生产应用必须调用已有鉴权系统验证凭证和登录撤销。
        return request ->
                "Bearer browser-test".equals(request.getHeader("Authorization"))
                        ? new EnigmaIdentity("42", "7", "demo-login", false, null)
                        : null;
    }

    public static ConfigurableApplicationContext start(int port) {
        var app = new SpringApplication(EnigmaDemoApplication.class);
        app.setBannerMode(Banner.Mode.OFF);
        app.setDefaultProperties(
                Map.of(
                        "server.port",
                        port,
                        "server.address",
                        "127.0.0.1",
                        "lfp.enigma.enabled",
                        true,
                        "lfp.enigma.store",
                        "memory",
                        "lfp.enigma.allow-insecure-localhost",
                        true,
                        "lfp.enigma.sessions-per-minute",
                        100,
                        "logging.level.root",
                        "WARN",
                        "spring.web.resources.static-locations",
                        "file:"
                                + java.nio.file.Path.of("../examples/enigma-browser")
                                        .toAbsolutePath()
                                + "/"));
        return app.run();
    }

    public static void main(String[] args) {
        start(args.length == 0 ? 18888 : Integer.parseInt(args[0]));
    }

    public record Person(@NotBlank String name, String secret) {}

    @RestController
    public static class SampleController {
        @GetMapping("/demo/async")
        public java.util.concurrent.Callable<JsonResult<?>> async() {
            return () -> JsonResult.json(0, "", "async");
        }

        @GetMapping("/demo/params/{id}")
        @EnigmaProtected(EnigmaProtected.Mode.SIGN)
        public JsonResult<?> params(
                @PathVariable String id, @RequestParam MultiValueMap<String, String> params) {
            calls.incrementAndGet();
            return JsonResult.json(0, "", Map.of("id", id, "params", params));
        }

        @PutMapping("/demo/params")
        @EnigmaProtected(EnigmaProtected.Mode.SIGN)
        public JsonResult<?> putParams(@RequestParam MultiValueMap<String, String> params) {
            calls.incrementAndGet();
            return JsonResult.json(0, "", params);
        }

        @PostMapping(
                value = "/demo/body",
                consumes = "application/json",
                produces = "application/json")
        @EnigmaProtected
        @JsonResultFilter(
                type = Person.class,
                include = {"name"})
        public ResponseEntity<JsonResult<?>> body(@Valid @RequestBody Person person) {
            calls.incrementAndGet();
            return ResponseEntity.status(201).body(JsonResult.json(0, "", person));
        }

        @GetMapping("/demo/error")
        @EnigmaProtected(EnigmaProtected.Mode.SIGN)
        public JsonResult<?> error() {
            calls.incrementAndGet();
            throw new IllegalStateException("internal-secret-must-not-leak");
        }

        @GetMapping("/demo/stats")
        public Map<String, Integer> stats() {
            return Map.of("calls", calls.get());
        }
    }
}
