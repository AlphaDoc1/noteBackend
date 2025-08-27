package com.example.notes.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document(collection = "user_activity")
public class UserActivity {

    @Id
    private String id;

    @Indexed
    private String username;

    private String action; // LOGIN, REGISTER, UPLOAD, DOWNLOAD

    private String details; // any extra info like file key, search query, counts

    private String ipAddress;

    @Indexed
    private Instant occurredAt = Instant.now();

    public UserActivity() {}

    public UserActivity(String username, String action, String details, String ipAddress) {
        this.username = username;
        this.action = action;
        this.details = details;
        this.ipAddress = ipAddress;
        this.occurredAt = Instant.now();
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }

    public String getDetails() { return details; }
    public void setDetails(String details) { this.details = details; }

    public String getIpAddress() { return ipAddress; }
    public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }

    public Instant getOccurredAt() { return occurredAt; }
    public void setOccurredAt(Instant occurredAt) { this.occurredAt = occurredAt; }
}


