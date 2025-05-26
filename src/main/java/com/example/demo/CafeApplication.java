package com.example.demo;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import com.example.demo.api.config.JwtProperties;

@EnableConfigurationProperties(JwtProperties.class)
@ComponentScan(basePackages = "com.example.demo.api")
@EnableJpaRepositories(basePackages = "com.example.demo.api.repository")
@EntityScan(basePackages = "com.example.demo.api.model")
@SpringBootApplication
public class CafeApplication {

    public static void main(String[] args) {
        SpringApplication.run(CafeApplication.class, args);
    }
}