package com.cs203.smucode.services.impl;

import com.cs203.smucode.models.Notification;
import com.cs203.smucode.repositories.NotificationRepository;
import com.cs203.smucode.services.INotificationService;
import jakarta.persistence.EntityNotFoundException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class NotificationServiceImpl implements INotificationService {

    private final NotificationRepository notificationRepository;

    @Autowired
    public NotificationServiceImpl(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    @Override
    public Notification createNotification(Notification notification) {
        notification.setCreatedAt(LocalDateTime.now());
        notification.setRead(false);
        return notificationRepository.save(notification);
    }

    @Override
    public List<Notification> getNotificationsByUsername(String username) {
        return notificationRepository.findByUsername(username);
    }

    @Override
    public List<Notification> getUnreadNotificationsByUsername(String username) {
        return notificationRepository.findByUsernameAndIsRead(username, false);
    }

    @Override
    public Notification markAsRead(UUID id) {
        Notification notification = notificationRepository.findById(id).
                orElseThrow(EntityNotFoundException::new);
        notification.setRead(true);
        return notificationRepository.save(notification);
    }

    @Override
    public Notification markAsUnread(UUID id) {
        Notification notification = notificationRepository.findById(id).
                orElseThrow(EntityNotFoundException::new);
        notification.setRead(false);
        return notificationRepository.save(notification);
    }
}
