package com.campusconnect.service;

import com.campusconnect.entity.Activity;
import com.campusconnect.entity.Notification;
import com.campusconnect.entity.Participation;
import com.campusconnect.entity.User;
import com.campusconnect.repository.ActivityRepository;
import com.campusconnect.repository.NotificationRepository;
import com.campusconnect.repository.ParticipationRepository;
import com.campusconnect.repository.UserRepository;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.util.List;

@Service
public class ParticipationService {
    private final ParticipationRepository participationRepository;
    private final ActivityRepository activityRepository;
    private final UserRepository userRepository;
    private final NotificationRepository notificationRepository;

    public ParticipationService(ParticipationRepository participationRepository, ActivityRepository activityRepository, UserRepository userRepository, NotificationRepository notificationRepository) {
        this.participationRepository = participationRepository;
        this.activityRepository = activityRepository;
        this.userRepository = userRepository;
        this.notificationRepository = notificationRepository;
    }

    public Participation registerForActivity(Long userId, Long activityId) {
        User user = userRepository.findById(userId).orElseThrow();
        Activity activity = activityRepository.findById(activityId).orElseThrow();

        if (participationRepository.findByUserAndActivity(user, activity).isPresent()) {
            throw new RuntimeException("Already registered for this activity");
        }

        if (activity.getCurrentParticipants() >= activity.getMaxParticipants()) {
            throw new RuntimeException("Activity is full");
        }

        Participation participation = new Participation();
        participation.setUser(user);
        participation.setActivity(activity);
        participation.setStatus(Participation.Status.REGISTERED);
        participation.setRegisteredAt(LocalDate.now());

        activity.setCurrentParticipants(activity.getCurrentParticipants() + 1);
        activityRepository.save(activity);

        Notification notification = new Notification();
        notification.setUser(user);
        notification.setMessage("Your registration for " + activity.getTitle() + " has been confirmed! 🎉");
        notification.setType("success");
        notification.setIsRead(false);
        notificationRepository.save(notification);

        return participationRepository.save(participation);
    }

    public List<Participation> getUserParticipations(Long userId) {
        User user = userRepository.findById(userId).orElseThrow();
        return participationRepository.findByUser(user);
    }

    public List<Participation> getAllParticipations() {
        return participationRepository.findAll();
    }

    public void cancelRegistration(Long userId, Long activityId) {
        User user = userRepository.findById(userId).orElseThrow();
        Activity activity = activityRepository.findById(activityId).orElseThrow();

        Participation participation = participationRepository.findByUserAndActivity(user, activity)
                .orElseThrow(() -> new RuntimeException("Registration not found"));

        participationRepository.delete(participation);

        activity.setCurrentParticipants(Math.max(0, activity.getCurrentParticipants() - 1));
        activityRepository.save(activity);

        Notification notification = new Notification();
        notification.setUser(user);
        notification.setMessage("You have successfully unregistered from " + activity.getTitle());
        notification.setType("info");
        notification.setIsRead(false);
        notificationRepository.save(notification);
    }
}
