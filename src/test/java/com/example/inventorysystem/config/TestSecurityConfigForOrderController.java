package com.example.inventorysystem.config;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;

@Configuration
@Profile("test-order-controller") // Active only in "test-order-controller" profile
public class TestSecurityConfigForOrderController {

    private static final Logger logger = LoggerFactory.getLogger(TestSecurityConfigForOrderController.class);

    // Custom filter for logging request URIs
    public class RequestLoggingFilter implements Filter {
        @Override
        public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
                throws IOException, ServletException {
            if (request instanceof HttpServletRequest) {
                HttpServletRequest httpRequest = (HttpServletRequest) request;
                logger.debug("Request secured: {}", httpRequest.getRequestURI());
            }
            chain.doFilter(request, response);
        }
    }

    @Bean(name = "testSecurityFilterChain")
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/orders/**").authenticated() // Restrict access to /api/orders/**
                .anyRequest().permitAll()) // Permit all other requests
            .addFilterBefore(new RequestLoggingFilter(), BasicAuthenticationFilter.class) // Add logging filter
            .httpBasic(httpBasic -> {}); // Use HTTP Basic authentication
        return http.build();
    }
}
