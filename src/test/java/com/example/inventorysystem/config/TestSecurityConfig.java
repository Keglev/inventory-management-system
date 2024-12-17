package com.example.inventorysystem.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class TestSecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable()) // Use Lambda DSL to disable CSRF
            .authorizeHttpRequests(auth -> auth.anyRequest().permitAll()); // Permit all requests for testing
        return http.build();
    }

    @Primary
    @Bean
    public PasswordEncoder passwordEncoder() {
        // Use BCryptPasswordEncoder for better testing practice
        return new BCryptPasswordEncoder();
    }
}
