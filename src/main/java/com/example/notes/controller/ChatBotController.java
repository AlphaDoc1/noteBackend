package com.example.notes.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@RestController
@RequestMapping("/api/chat")
public class ChatBotController {

    // Read values from application.properties
    @Value("${gemini.api.key}")
    private String apiKey;

    @Value("${gemini.api.url}")
    private String geminiApiUrl;

    @Autowired
    private RestTemplate restTemplate;

    @PostMapping("/ask")
    public ResponseEntity<?> askQuestion(@RequestBody Map<String, String> payload) {

        String message = payload.get("message");
        if (message == null || message.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Message cannot be empty"));
        }

        // Build the Gemini request payload
        Map<String, Object> requestBody = Map.of(
            "contents", List.of(
                Map.of("parts", List.of(
                    Map.of("text", message)
                ))
            )
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        try {
            // Build API URL dynamically with your secure key
            String urlWithKey = geminiApiUrl + "?key=" + apiKey;

            ResponseEntity<Map> response =
                    restTemplate.postForEntity(urlWithKey, entity, Map.class);

            if (response.getBody() == null) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(Map.of("error", "Gemini API returned a null response"));
            }

            return ResponseEntity.ok(response.getBody());

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error calling Gemini API: " + e.getMessage()));
        }
    }
}
