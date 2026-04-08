package com.campusconnect.service;

import com.campusconnect.entity.Activity;
import com.campusconnect.entity.User;
import com.campusconnect.repository.ActivityRepository;
import com.campusconnect.repository.UserRepository;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class ActivityService {
    private final ActivityRepository activityRepository;
    private final UserRepository userRepository;

    public ActivityService(ActivityRepository activityRepository, UserRepository userRepository) {
        this.activityRepository = activityRepository;
        this.userRepository = userRepository;
    }

    public List<Activity> getAllActivities() {
        return activityRepository.findAll();
    }

    public Activity getActivityById(Long id) {
        return activityRepository.findById(id).orElseThrow(() -> new RuntimeException("Activity not found"));
    }

    public Activity createActivity(Activity activity, String email) {
        User admin = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Admin user not found"));
        
        activity.setCreatedBy(admin);
        
        // Ensure default data points so frontend doesn't crash if omitted
        if (activity.getCurrentParticipants() == null) activity.setCurrentParticipants(0);
        if (activity.getColor() == null) activity.setColor("#7c3aed");
        if (activity.getGradient() == null) activity.setGradient("linear-gradient(135deg,#7c3aed,#4f46e5)");
        if (activity.getFeatured() == null) activity.setFeatured(false);
        
        return activityRepository.save(activity);
    }

    public Activity updateActivity(Long id, Activity activityDetails) {
        Activity activity = getActivityById(id);
        activity.setTitle(activityDetails.getTitle());
        activity.setDescription(activityDetails.getDescription());
        activity.setCategory(activityDetails.getCategory());
        activity.setDate(activityDetails.getDate());
        activity.setTime(activityDetails.getTime());
        activity.setLocation(activityDetails.getLocation());
        activity.setMaxParticipants(activityDetails.getMaxParticipants());
        activity.setColor(activityDetails.getColor());
        activity.setGradient(activityDetails.getGradient());
        activity.setTags(activityDetails.getTags());
        activity.setFeatured(activityDetails.getFeatured());
        return activityRepository.save(activity);
    }

    public void deleteActivity(Long id) {
        activityRepository.deleteById(id);
    }
}
