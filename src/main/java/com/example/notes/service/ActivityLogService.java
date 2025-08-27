package com.example.notes.service;

import com.example.notes.model.UserActivity;
import com.example.notes.repository.UserActivityRepository;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;

@Service
public class ActivityLogService {

    private final UserActivityRepository repository;

    public ActivityLogService(UserActivityRepository repository) {
        this.repository = repository;
    }

    public void log(HttpServletRequest request, String username, String action, String details) {
        try {
            String ip = resolveIp(request);
            UserActivity activity = new UserActivity(username, action, details, ip);
            repository.save(activity);
        } catch (Exception ignored) {
            // Avoid blocking main flow due to logging failure
        }
    }

    private String resolveIp(HttpServletRequest request) {
        if (request == null) return null;
        String h = request.getHeader("x-forwarded-for");
        if (h != null && !h.isEmpty()) return h.split(",")[0].trim();
        return request.getRemoteAddr();
    }
}


