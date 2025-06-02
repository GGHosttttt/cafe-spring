
package com.example.demo.api.controller;

import com.example.demo.api.dto.ApiResponse;
import com.example.demo.api.dto.LoginRequest;
import com.example.demo.api.dto.LoginResponse;
import com.example.demo.api.model.TokenBlacklist;
import com.example.demo.api.model.User;
import com.example.demo.api.repository.TokenBlacklistRepository;
import com.example.demo.api.repository.UserRepository;
import com.example.demo.api.service.JwtService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Autowired
    private TokenBlacklistRepository tokenBlacklistRepository;
    
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest loginRequest) {
        logger.debug("Login attempt for username: {}", loginRequest.getUsername());
        try {
            User user = userRepository.findByUsername(loginRequest.getUsername())
                    .orElseThrow(() -> {
                        logger.error("User not found: {}", loginRequest.getUsername());
                        return new RuntimeException("User not found");
                    });
            logger.debug("Stored hash for {}: {}", loginRequest.getUsername(), user.getPassword());
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));
            String token = jwtService.generateToken(user.getUsername(), user.getRole());
            LoginResponse response = new LoginResponse();
            response.setToken(token);
            logger.info("Login successful for username: {}", loginRequest.getUsername());
            return ResponseEntity.ok(ApiResponse.success(response, "Login successful"));
        } catch (AuthenticationException e) {
            logger.error("Login failed for username: {}. Reason: {}", loginRequest.getUsername(), e.getMessage());
            return ResponseEntity.status(401).body(ApiResponse.error("Invalid credentials", null));
        }
    }

    @DeleteMapping("/logout")
    public ResponseEntity<ApiResponse<String>> logout(@RequestHeader("Authorization") String authHeader) {
        logger.debug("Logout attempt with token: {}", authHeader);
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            logger.warn("Invalid or missing token during logout");
            return ResponseEntity.status(401).body(ApiResponse.error("Invalid or missing token", null));
        }

        String token = authHeader.substring(7);
        if (tokenBlacklistRepository.existsById(token)) {
            logger.warn("Blacklisted token used for logout: {}", token);
            return ResponseEntity.status(401).body(ApiResponse.error("Token is blacklisted", null));
        }

        String username = jwtService.extractUsername(token);
        if (username != null && jwtService.validateToken(token, username)) {
            // Blacklist the token
            TokenBlacklist blacklistedToken = new TokenBlacklist();
            blacklistedToken.setToken(token);
            blacklistedToken.setExpiryDate(jwtService.extractExpiration(token).toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDateTime());
            tokenBlacklistRepository.save(blacklistedToken);
            logger.info("Logout successful for username: {}. Token blacklisted: {}", username, token);
            return ResponseEntity.ok(ApiResponse.success(null, "Logout successful: Token invalidated"));
        } else {
            logger.warn("Invalid token during logout for username: {}", username);
            return ResponseEntity.status(401).body(ApiResponse.error("Invalid token", null));
        }
    }

    @PostMapping("/test-password")
    public ResponseEntity<ApiResponse<String>> testPassword(@Valid @RequestBody LoginRequest loginRequest) {
        logger.debug("Processing request for username: {}", loginRequest.getUsername());

        // Validate input
        if (loginRequest.getUsername() == null || loginRequest.getUsername().trim().isEmpty()) {
            logger.warn("Invalid request: Username is required");
            return ResponseEntity.status(400).body(ApiResponse.error("Username is required", null));
        }
        if (loginRequest.getPassword() == null || loginRequest.getPassword().trim().isEmpty()) {
            logger.warn("Invalid request: Password is required");
            return ResponseEntity.status(400).body(ApiResponse.error("Password is required", null));
        }

        try {
            // Check if user already exists
            Optional<User> existingUser = userRepository.findByUsername(loginRequest.getUsername());
            User user = existingUser.orElse(new User());

            // Generate bcrypt hash for the password
            String newHash = passwordEncoder.encode(loginRequest.getPassword());
            logger.debug("Generated hash for {}: {}", loginRequest.getUsername(), newHash);

            // Set user details
            user.setUsername(loginRequest.getUsername());
            user.setPassword(newHash);
            if (user.getId() == null) { // New user
                user.setRole("ADMIN"); // Default role for new users
                logger.debug("Creating new user with username: {}", loginRequest.getUsername());
            } else {
                logger.debug("Updating existing user with username: {}", loginRequest.getUsername());
            }

            // Save user to the database
            userRepository.save(user);
            logger.info("User {} saved with hash: {}", loginRequest.getUsername(), newHash);

            // Verify hash (optional, for testing purposes)
            boolean matches = passwordEncoder.matches(loginRequest.getPassword(), newHash);
            String result = "Password matches: " + matches + ", Hash: " + newHash;
            logger.debug("Test result: {}", result);

            String message = user.getId() == null ? "User created successfully" : "User password updated successfully";
            return ResponseEntity.ok(ApiResponse.success(result, message));
        } catch (Exception e) {
            logger.error("Failed to process user {}: {}", loginRequest.getUsername(), e.getMessage());
            return ResponseEntity.status(500).body(ApiResponse.error("Failed to process user: " + e.getMessage(), null));
        }
    }

}