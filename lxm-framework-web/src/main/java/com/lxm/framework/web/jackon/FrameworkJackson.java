package com.lxm.framework.web.jackon;

import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.module.SimpleModule;
import tools.jackson.databind.ext.javatime.deser.*;
import tools.jackson.databind.ext.javatime.ser.*;
import java.time.*;
import java.time.format.DateTimeFormatter;

public final class FrameworkJackson {
    private FrameworkJackson() {}

    public static SimpleModule dates() {
        var module = new SimpleModule("lxm-json");
        var time = DateTimeFormatter.ofPattern("HH:mm:ss");
        var date = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        var datetime = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        module.addSerializer(LocalTime.class, new LocalTimeSerializer(time));
        module.addSerializer(LocalDate.class, new LocalDateSerializer(date));
        module.addSerializer(LocalDateTime.class, new LocalDateTimeSerializer(datetime));
        module.addDeserializer(LocalTime.class, new LocalTimeDeserializer(time));
        module.addDeserializer(LocalDate.class, new LocalDateDeserializer(date));
        module.addDeserializer(LocalDateTime.class, new LocalDateTimeDeserializer(datetime));
        return module;
    }

    public static JsonMapper mapper() {
        return JsonMapper.builder()
                .defaultDateFormat(new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss"))
                .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
                .addModule(JacksonConfig.module())
                .build();
    }
}
