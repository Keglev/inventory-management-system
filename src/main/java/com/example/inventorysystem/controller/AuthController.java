package com.example.inventorysystem.controller;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import com.example.inventorysystem.model.User;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.inventorysystem.security.JwtUtils;
import com.example.inventorysystem.service.UserService;


@RestController
@RequestMapping("/api/auth")
public class AuthController {
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

        userService.registerUser(username, email, password, role);
        return ResponseEntity.ok("User registered successfully");
    }


    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody Map<String, String> loginDetails) {
        String username = loginDetails.get("username");
        String password = loginDetails.get("password");
    
        if (userService.authenticateUser(username, password)) {
            User user = userService.getUserByUsername(username); // Retrieve user details
            String token = jwtUtils.generateToken(username, user.getRole()); // Pass role to generateToken
            return ResponseEntity.ok(token);
        } else {
            return ResponseEntity.status(401).body("Invalid username or password");
        }
    }
    
}
