package com.cs203.smucode.services.impl;

import com.cs203.smucode.models.Notification;
import com.cs203.smucode.repositories.NotificationRepository;
import com.cs203.smucode.services.INotificationService;
import jakarta.persistence.EntityNotFoundException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Service
public class NotificationServiceImpl implements INotificationService {

    private static final Logger logger = LoggerFactory.getLogger(NotificationServiceImpl.class);
    @Getter
    private final Map<String, SseEmitter> emitters = new ConcurrentHashMap<>();
    private final NotificationRepository notificationRepository;

    @Autowired
    public NotificationServiceImpl(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    @Override
    public Notification createNotification(Notification notification) {
        // TODO: move default values to DB
        notification.setCreatedAt(LocalDateTime.now());
        notification.setIsRead(false);
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
        notification.setIsRead(true);
        logger.info("notification marked as read: {}", notification);
        return notificationRepository.save(notification);
    }

    @Override
    public Notification markAsUnread(UUID id) {
        Notification notification = notificationRepository.findById(id).
                orElseThrow(EntityNotFoundException::new);
        notification.setIsRead(false);
        return notificationRepository.save(notification);
    }

    /**
     * Method to handle client subscribing to notification service
     *
     * @param username new subscriber
     */
    public SseEmitter subscribe(String username) {
        SseEmitter emitter = new SseEmitter(Long.MAX_VALUE); // Keep connection open indefinitely
        emitters.put(username, emitter); // Include new client into map of emitters

        // Graceful shutdown
        emitter.onCompletion(() -> emitters.remove(username));
        emitter.onTimeout(() -> emitters.remove(username));
        emitter.onError(e -> {
            emitters.remove(username);  // Clean up the emitter
            emitter.completeWithError(e);  // Close the emitter with the error
        });

        return emitter;
    }

}
