package com.campusconnect.service;

import com.campusconnect.entity.Notification;
import com.campusconnect.entity.User;
import com.campusconnect.repository.NotificationRepository;
import com.campusconnect.repository.UserRepository;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class NotificationService {
    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    public NotificationService(NotificationRepository notificationRepository, UserRepository userRepository) {
        this.notificationRepository = notificationRepository;
        this.userRepository = userRepository;
    }

    public List<Notification> getNotificationsForUser(Long userId) {
        User user = userRepository.findById(userId).orElseThrow();
        return notificationRepository.findByUserOrderByCreatedAtDesc(user);
    }

    public void markAsRead(Long id) {
        Notification notification = notificationRepository.findById(id).orElseThrow();
        notification.setIsRead(true);
        notificationRepository.save(notification);
    }

    public void markAllAsRead(Long userId) {
        User user = userRepository.findById(userId).orElseThrow();
        List<Notification> notifications = notificationRepository.findByUserOrderByCreatedAtDesc(user);
        notifications.forEach(n -> n.setIsRead(true));
        notificationRepository.saveAll(notifications);
    }

    public void broadcastNotification(String message, String type) {
        List<User> students = userRepository.findAll().stream()
                .filter(u -> u.getRole() == User.Role.STUDENT)
                .toList();

        List<Notification> notifications = students.stream().map(student -> {
            Notification notification = new Notification();
            notification.setUser(student);
            notification.setMessage(message);
            notification.setType(type);
            notification.setIsRead(false);
            return notification;
        }).toList();

        notificationRepository.saveAll(notifications);
    }
}
