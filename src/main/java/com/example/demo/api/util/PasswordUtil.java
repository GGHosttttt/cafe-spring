
package com.example.demo.api.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class PasswordUtil {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String password = "admin123";
        
        // Generate new hash
        String newHash = encoder.encode(password);
        System.out.println("Password: " + password);
        System.out.println("New Bcrypt Hash: " + newHash);
        System.out.println("New Hash Matches: " + encoder.matches(password, newHash));
    }
}