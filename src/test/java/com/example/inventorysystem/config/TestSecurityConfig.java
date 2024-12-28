// mvn test -Dtest=TestSecurityConfig
// all tests are ok
package com.example.inventorysystem.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
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
}
