package com.brunofontenele.ems.notification.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.brunofontenele.ems.notification.domain.Notification;
import com.brunofontenele.ems.notification.repository.NotificationRepository;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class NotificationService {
    
    private final NotificationRepository notificationRepository;

    public void sendNotification(String recipientEmail, String message) {
        notificationRepository.save(new Notification(recipientEmail, message));
    }

    @Transactional(readOnly = true)
    public List<Notification> getUserNotifications(String userEmail) {
        return notificationRepository.findByRecipientEmailOrderByCreatedAtDesc(userEmail);
    }

    public void markAsRead(Long id, String userEmail) {
        notificationRepository.findById(id).ifPresent(notification -> {
            if (notification.getRecipientEmail().equals(userEmail)) {
                notification.setRead(true);
                notificationRepository.save(notification);
            }
        });
    }

    public void markAllAsRead(String userEmail) {
        List<Notification> unreadNotifications = notificationRepository.findByRecipientEmailAndIsReadFalse(userEmail);
        unreadNotifications.forEach(notification -> notification.setRead(true));
        notificationRepository.saveAll(unreadNotifications);
}
}