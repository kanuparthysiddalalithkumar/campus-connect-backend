package com.campusconnect.controller;

import com.campusconnect.entity.Participation;
import com.campusconnect.service.ParticipationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/participation")
public class ParticipationController {
    private final ParticipationService participationService;

    public ParticipationController(ParticipationService participationService) {
        this.participationService = participationService;
    }

    @PostMapping("/{userId}/{activityId}")
    public ResponseEntity<Participation> register(@PathVariable Long userId, @PathVariable Long activityId) {
        return ResponseEntity.ok(participationService.registerForActivity(userId, activityId));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Participation>> getUserParticipations(@PathVariable Long userId) {
        return ResponseEntity.ok(participationService.getUserParticipations(userId));
    }

    @GetMapping
    public ResponseEntity<List<Participation>> getAllParticipations() {
        return ResponseEntity.ok(participationService.getAllParticipations());
    }

    @DeleteMapping("/{userId}/{activityId}")
    public ResponseEntity<Void> unregister(@PathVariable Long userId, @PathVariable Long activityId) {
        participationService.cancelRegistration(userId, activityId);
        return ResponseEntity.noContent().build();
    }
}
