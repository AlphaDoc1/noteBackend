package com.example.notes.config;

import com.example.notes.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import java.util.concurrent.CompletableFuture;

@Configuration
public class AppConfig {
    
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    // Warm up MongoDB connection pool on startup to reduce first-request latency
    @Bean
    public CommandLineRunner warmupMongo(UserRepository userRepository) {
        return args -> CompletableFuture.runAsync(() -> {
            try {
                userRepository.count();
            } catch (Exception ignored) {
            }
        });
    }
}
