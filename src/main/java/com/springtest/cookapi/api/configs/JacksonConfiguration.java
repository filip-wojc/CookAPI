package com.springtest.cookapi.api.configs;

import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.ArrayList;
import java.util.List;

@Configuration
public class JacksonConfiguration implements WebMvcConfigurer {
    @Override
    public void configureMessageConverters(List<HttpMessageConverter<?>> messageConverters) {
        messageConverters.stream()
                .filter(MappingJackson2HttpMessageConverter.class::isInstance)
                .map(MappingJackson2HttpMessageConverter.class::cast)
                .forEach(conv -> {
                    List<MediaType> supportedMediaTypes = new ArrayList<>(conv.getSupportedMediaTypes());
                    supportedMediaTypes.add(MediaType.APPLICATION_OCTET_STREAM);
                    conv.setSupportedMediaTypes(supportedMediaTypes);
                });
    }
}