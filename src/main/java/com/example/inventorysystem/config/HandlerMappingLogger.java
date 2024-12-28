package com.example.inventorysystem.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class HandlerMappingLogger implements HandlerInterceptor {

    private static final Logger logger = LoggerFactory.getLogger(HandlerMappingLogger.class);

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        logger.debug("Mapped handler: {}", handler);
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        if (ex != null) {
            logger.error("Handler encountered exception: {}", ex.getMessage(), ex);
        }
        logger.debug("Request completed for handler: {}", handler);
    }
}

