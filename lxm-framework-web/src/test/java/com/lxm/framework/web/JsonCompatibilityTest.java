package com.lxm.framework.web;

import com.lxm.framework.web.jackon.FrameworkJackson;
import com.lxm.framework.web.jsonresult.JsonResult;
import com.lxm.framework.common.web.Result;
import com.lxm.framework.web.jsonresult.annotation.JsonResultFilter;
import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

class JsonCompatibilityTest {
    static class Broken {
        public String getValue() {
            throw new IllegalStateException("fixture failure");
        }
    }

    @Test
    void serializationFailurePropagatesAndDoesNotPolluteNextResult() {
        var mapper = FrameworkJackson.mapper();
        assertThrows(
                RuntimeException.class,
                () -> mapper.writeValueAsString(JsonResult.json(0, "", new Broken())));
        assertEquals(
                "ok",
                mapper.readTree(
                                mapper.writeValueAsString(
                                        JsonResult.json(0, "", Map.of("value", "ok"))))
                        .path("data")
                        .path("value")
                        .asString());
    }

    record Person(String name, String secret) {}

    @JsonResultFilter(
            type = Person.class,
            include = {"name"})
    void filtered() {}

    @Test
    void matchesUnmodifiedJava17SourceFixtures() throws Exception {
        var mapper = FrameworkJackson.mapper();
        var values = new LinkedHashMap<String, Object>();
        values.put("success", JsonResult.json(0, "ok", Map.of("name", "中文", "id", 123)));
        values.put("failure", JsonResult.json(500, "业务错误", null));
        values.put("null", JsonResult.json());
        var signed = JsonResult.json(0, "", Map.of("id", 123));
        signed.setSign("test-sign");
        values.put("signed", signed);
        values.put("date", JsonResult.json(0, "", LocalDateTime.of(2026, 10, 8, 12, 0)));
        var filtered = JsonResult.json(0, "", new Person("公开", "秘密"));
        filtered.filter(
                getClass().getDeclaredMethod("filtered").getAnnotation(JsonResultFilter.class));
        values.put("filtered", filtered);
        values.put(
                "nested", JsonResult.json(0, "", JsonResult.json(0, "", Map.of("nested", true))));
        values.put("result", Result.of(0, "", Map.of("id", 123)));
        for (var entry : values.entrySet()) {
            try (var sample =
                    getClass().getResourceAsStream("/legacy-json/" + entry.getKey() + ".json")) {
                assertNotNull(sample);
                assertEquals(
                        mapper.readTree(sample),
                        mapper.readTree(mapper.writeValueAsString(entry.getValue())),
                        entry.getKey());
            }
        }
    }

    @Test
    void filtersAreScopedAndInputMapIsNotModified() {
        var mapper = FrameworkJackson.mapper();
        var source = Map.<String, Object>of("code", 0, "message", "ok", "name", "value");
        var result = JsonResult.json(source);
        assertEquals(3, source.size());
        assertEquals(Map.of("name", "value"), result.getData());
        var filtered = JsonResult.json(0, "", new Person("公开", "秘密"));
        filtered.include(Person.class, new String[] {"name"});
        assertFalse(
                mapper.readTree(mapper.writeValueAsString(filtered)).path("data").has("secret"));
        assertTrue(
                mapper.readTree(
                                mapper.writeValueAsString(
                                        JsonResult.json(0, "", new Person("公开", "秘密"))))
                        .path("data")
                        .has("secret"));
    }
}
