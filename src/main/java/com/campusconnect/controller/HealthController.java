package com.campusconnect.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Health check endpoint — used by Railway to verify the service is alive,
 * and by the frontend to test connectivity.
 * Publicly accessible (configured in SecurityConfig).
 */
@RestController
@RequestMapping("/api")
public class HealthController {

    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of(
            "status", "UP",
            "service", "campus-connect-backend",
            "message", "Backend is running successfully"
        ));
    }
}
