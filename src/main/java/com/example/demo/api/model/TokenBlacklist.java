package com.example.demo.api.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Data
public class TokenBlacklist {
    @Id
    private String token;
    private LocalDateTime expiryDate;
}
