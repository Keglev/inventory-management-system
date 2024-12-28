package com.example.inventorysystem.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer{

    private final HandlerMappingLogger handlerMappingLogger;

    public WebMvcConfig(HandlerMappingLogger handlerMappingLogger) {
        this.handlerMappingLogger = handlerMappingLogger;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(handlerMappingLogger);
    }
    
}
