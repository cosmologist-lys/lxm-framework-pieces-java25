package com.lxm.framework.web.jackon;

import com.lxm.framework.web.jsonresult.JsonResult;
import com.lxm.framework.web.jsonresult.JsonResultSerializer;
import com.lxm.framework.web.jsonresult.filter.JsonResultProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tools.jackson.databind.*;
import tools.jackson.databind.module.SimpleModule;
import tools.jackson.databind.ser.*;
import java.util.List;

@Configuration(proxyBeanMethods = false)
public class JacksonConfig {
    @Bean
    public SimpleModule lxmJsonModule() { return module(); }

    public static SimpleModule module() {
        var module = FrameworkJackson.dates();
        module.addSerializer(new JsonResultSerializer());
        module.setSerializerModifier(new ValueSerializerModifier() {
            @Override
            public List<BeanPropertyWriter> changeProperties(SerializationConfig config, BeanDescription.Supplier bean,
                                                            List<BeanPropertyWriter> properties) {
                return properties.stream().map(FilteredWriter::new).map(BeanPropertyWriter.class::cast).collect(java.util.stream.Collectors.toCollection(java.util.ArrayList::new));
            }
        });
        return module;
    }

    private static class FilteredWriter extends BeanPropertyWriter {
        FilteredWriter(BeanPropertyWriter writer) { super(writer); }
        @Override
        public void serializeAsProperty(Object bean, tools.jackson.core.JsonGenerator generator, SerializationContext context) throws Exception {
            var filter = (JsonResultProvider) context.getAttribute(JsonResultSerializer.FILTER_ATTRIBUTE);
            if (filter == null || filter.allows(bean.getClass(), getName())) super.serializeAsProperty(bean, generator, context);
        }
    }
}
