
package com.example.demo.api.repository;

import com.example.demo.api.model.TokenBlacklist;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TokenBlacklistRepository extends JpaRepository<TokenBlacklist, String> {
}



