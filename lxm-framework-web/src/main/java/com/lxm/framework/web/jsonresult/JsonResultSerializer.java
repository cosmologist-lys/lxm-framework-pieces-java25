package com.lxm.framework.web.jsonresult;

import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ValueSerializer;

/** 只在当前 SerializationContext 设置过滤状态；异常直接向转换器传播。 */
public class JsonResultSerializer extends ValueSerializer<JsonResult<?>> {
    public static final String FILTER_ATTRIBUTE = JsonResultSerializer.class.getName();

    @Override
    public Class<?> handledType() {
        return JsonResult.class;
    }

    @Override
    public void serialize(
            JsonResult<?> value, JsonGenerator generator, SerializationContext context) {
        Object previous = context.getAttribute(FILTER_ATTRIBUTE);
        if (value.getJsonResultProvider() != null)
            context.setAttribute(FILTER_ATTRIBUTE, value.getJsonResultProvider());
        try {
            generator.writeStartObject();
            generator.writeNumberProperty("code", value.getCode());
            generator.writeStringProperty("message", value.getMessage());
            generator.writeName("data");
            context.writeValue(generator, value.getData());
            if (value.getSign() != null && !value.getSign().isBlank())
                generator.writeStringProperty("sign", value.getSign());
            generator.writeEndObject();
        } finally {
            context.setAttribute(FILTER_ATTRIBUTE, previous);
        }
    }
}
