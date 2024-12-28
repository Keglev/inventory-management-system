// mvn test -Dtest=AuthenticationAndRegistrationTest
// All tests passed

package com.example.inventorysystem.controller;

import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.inventorysystem.config.TestSecurityConfig;
import com.example.inventorysystem.model.User;
import com.example.inventorysystem.security.JwtUtils;
import com.example.inventorysystem.service.UserService;

@WebMvcTest(controllers = AuthController.class)
@ContextConfiguration(classes = {AuthController.class, TestSecurityConfig.class}) 
public class AuthenticationAndRegistrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @MockBean
    private JwtUtils jwtUtils;

    @Test
    void testAuthenticateUser() throws Exception {

        String loginPayload = """
                {
                    "username": "user",
                    "password": "password123"
                }
                """;

        // Mock UserService authentication and user retrieval
        when(userService.authenticateUser("user", "password123")).thenReturn(true);
        when(userService.getUserByUsername("user")).thenReturn(
                User.builder()
                    .username("user")
                    .role("USER").build()
        );
        when(jwtUtils.generateToken("user", "USER")).thenReturn("mock-jwt-token");

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(loginPayload))
                .andExpect(status().isOk());
    }

    @Test
    void testAuthenticateAdmin() throws Exception {
        String loginPayload = """
                {
                    "username": "admin",
                    "password": "adminpassword"
                }
                """;

        // Mock authentication and user retrieval
        when(userService.authenticateUser("admin", "adminpassword")).thenReturn(true);
        when(userService.getUserByUsername("admin")).thenReturn(
                User.builder()
                    .username("admin")
                    .role("ADMIN")
                    .build()
        );
    when(jwtUtils.generateToken("admin", "ADMIN")).thenReturn("mock-jwt-token");

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(loginPayload))
                .andExpect(status().isOk());
    }

    @Test
    void testRegisterUser() throws Exception {
        String registerPayload = """
                {
                    "username": "newUser",
                    "email": "newuser@example.com",
                    "password": "password123",
                    "role": "USER"
                }
                """;
         // Mock the User object returned by UserService
         when(userService.registerUser("newUser", "newuser@example.com", "password123", "USER"))
            .thenReturn(User.builder()
                            .username("newUser")
                            .email("newuser@example.com")
                            .password("password123")
                            .role("USER")
                            .build());

        mockMvc.perform(post("/api/auth/register")
               .contentType(MediaType.APPLICATION_JSON)
               .content(registerPayload))
               .andExpect(status().isOk()); // Adjusted to match your controller behavior
    }

    @Test
    void testRegisterAdmin() throws Exception {
        String registerPayload = """
                {
                    "username": "newAdmin",
                    "email": "newadmin@example.com",
                    "password": "adminpassword",
                    "role": "ADMIN"
                }
                """;
        // Mock the User object returned by UserService
        User mockAdmin = User.builder()
                             .username("newAdmin")
                             .email("newadmin@example.com")
                             .password("adminpassword")
                             .role("ADMIN")
                             .build();

        when(userService.registerUser(anyString(), anyString(), anyString(), anyString()))
                        .thenReturn(mockAdmin); // Return the User object instead of true

        mockMvc.perform(post("/api/auth/register")
               .contentType(MediaType.APPLICATION_JSON)
               .content(registerPayload))
               .andExpect(status().isOk());
    }
}