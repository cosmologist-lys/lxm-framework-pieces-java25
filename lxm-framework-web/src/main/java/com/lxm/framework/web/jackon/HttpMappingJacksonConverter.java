package com.lxm.framework.web.jackon;

import tools.jackson.databind.json.JsonMapper;
import org.springframework.http.converter.json.JacksonJsonHttpMessageConverter;



public class HttpMappingJacksonConverter extends JacksonJsonHttpMessageConverter {

    public HttpMappingJacksonConverter(JsonMapper objectMapper) {
        super(objectMapper);
    }
}
