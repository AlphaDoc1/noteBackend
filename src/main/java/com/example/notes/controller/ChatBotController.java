package com.example.notes.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@RestController
@RequestMapping("/api/chat")
public class ChatBotController {

    // Use your provided Gemini API key and URL for the Gemini 2.0 Flash model.
    private static final String GEMINI_API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent?key=AIzaSyCMXqZnrDTJIl_1nNLqFQCVkT6Ng1o8IrQ";

    @Autowired
    private RestTemplate restTemplate;

    @PostMapping("/ask")
    public ResponseEntity<?> askQuestion(@RequestBody Map<String, String> payload) {
        String message = payload.get("message");
        if (message == null || message.trim().isEmpty()) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Message cannot be empty");
            return ResponseEntity.badRequest().body(errorResponse);
        }
        
        // Build the payload as required by the Gemini API reference.
        // Reference payload:
        // {
        //   "contents": [{
        //     "parts": [{"text": "Explain how AI works"}]
        //   }]
        // }
        Map<String, Object> textPart = new HashMap<>();
        textPart.put("text", message);
        
        List<Map<String, Object>> parts = new ArrayList<>();
        parts.add(textPart);
        
        Map<String, Object> contentItem = new HashMap<>();
        contentItem.put("parts", parts);
        
        List<Map<String, Object>> contents = new ArrayList<>();
        contents.add(contentItem);
        
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("contents", contents);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(GEMINI_API_URL, entity, Map.class);
            System.out.println("Gemini API Response: " + response.getBody());
            if (response.getBody() == null) {
                Map<String, String> errorResponse = new HashMap<>();
                errorResponse.put("error", "Gemini API returned a null response");
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
            }
            return ResponseEntity.ok(response.getBody());
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Error calling Gemini API: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
}
