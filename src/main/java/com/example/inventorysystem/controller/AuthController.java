package com.example.inventorysystem.controller;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.inventorysystem.model.User;
import com.example.inventorysystem.security.JwtUtils;
import com.example.inventorysystem.service.UserService;


@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    private final UserService userService;
    private final JwtUtils jwtUtils;

    public AuthController(UserService userService, JwtUtils jwtUtils) {
        this.userService = userService;
        this.jwtUtils = jwtUtils;
    }

    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody Map<String, String> userDetails) {
        String username = userDetails.get("username");
        String email = userDetails.get("email");
        String password = userDetails.get("password");
        String role = userDetails.get("role");

        log.debug("Registering user: {}", username);
        userService.registerUser(username, email, password, role);

        log.info("User {} registered successfully with role {}", username, role);
        return ResponseEntity.ok("User registered successfully");
    }


    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody Map<String, String> loginDetails) {
        String username = loginDetails.get("username");
        String password = loginDetails.get("password");
        
        log.debug("Authenticating user: {}", username);
        if (userService.authenticateUser(username, password)) {
            User user = userService.getUserByUsername(username); // Retrieve user details
            String token = jwtUtils.generateToken(username, user.getRole()); // Pass role to generateToken
            log.info("User {} authenticated successfully. Token generated.", username);

            return ResponseEntity.ok(token);
        } else {
            log.warn("Authentication failed for user {}", username);
            return ResponseEntity.status(401).body("Invalid username or password");
        }
    }
    
}
