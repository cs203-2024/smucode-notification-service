package com.cs203.smucode.controllers;

import com.cs203.smucode.dto.NotificationDTO;
import com.cs203.smucode.exception.ApiRequestException;
import com.cs203.smucode.handlers.EventHandler;
import com.cs203.smucode.mappers.NotificationMapper;
import com.cs203.smucode.models.Notification;
import com.cs203.smucode.services.INotificationService;
import java.util.List;
import java.util.UUID;

import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/notifications")
public class NotificationController {

    private static final Logger logger = LoggerFactory.getLogger(NotificationController.class);

    private final NotificationMapper notificationMapper;
    private final INotificationService notificationService;
    private final EventHandler eventHandler;

    @Autowired
    public NotificationController(INotificationService notificationService,
                                  NotificationMapper notificationMapper, EventHandler eventHandler) {
        this.notificationService = notificationService;
        this.notificationMapper = notificationMapper;
        this.eventHandler = eventHandler;
    }

    /**
     * Endpoint to handle incoming notifications from event services.
     *
     * <p>This method receives a notification payload, validates and converts it,
     * and then triggers any associated event handling logic.
     * It persists the notification and sends it to relevant subscribers in real-time.
     * </p>
     *
     * @param notificationDTO The notification DTO received from
     *                        the event-sending service, containing details such
     *                        as message type, recipient information, and content.
     * @return ResponseEntity containing the created Notification object if successful,
     *         along with an HTTP 200 OK status.
     *
     * @throws ApiRequestException if the notification type is invalid or
     *         if any error occurs during notification creation or processing.
     */
    @PostMapping("/stream")
    public ResponseEntity<Notification> createNotification(
        @RequestBody @Valid NotificationDTO notificationDTO
    ) {
        try {
            Notification notification = notificationMapper.notificationDTOtoNotification(notificationDTO);
            notificationService.createNotification(notification);

            logger.info("Notification created: {}", notification);
            // Handle incoming event, eg. notify subscribed users
            eventHandler.handleEvent(notification);
            return ResponseEntity.ok(notification);

        } catch (IllegalArgumentException e) {
            throw new ApiRequestException("Invalid notification type");
        } catch (Exception e) {
            throw new ApiRequestException("Something went wrong creating a notification; " + e.getMessage());
        }
    }

    /**
     * Endpoint to subscribe user to notification service
     *
     * @param username
     * @return SseEmitter which keeps the connection open and streams incoming notifications
     */
    @GetMapping("/subscribe/{username}")
    public SseEmitter subscribe(@PathVariable String username) {
        return notificationService.subscribe(username);
    }

    /**
     * Endpoint to get notifications for user
     *
     * @param username
     * @return
     */
    @GetMapping("/{username}")
    public ResponseEntity<List<Notification>> getNotificationsByUsername(@PathVariable String username) {
        try {
            return ResponseEntity.ok(
                    notificationService.getNotificationsByUsername(username)
            );
        } catch (Exception e) {
            throw new ApiRequestException("Something went wrong getting the notifications");
        }
    }

    /**
     * Endpoint to get all unread notifications for user
     *
     * @param username
     * @return
     */
    @GetMapping("/{username}/unread")
    public ResponseEntity<List<Notification>> getUnreadNotificationsByUsername(
        @PathVariable String username
    ) {
        try {
            return ResponseEntity.ok(
                    notificationService.getUnreadNotificationsByUsername(username)
            );
        } catch (Exception e) {
            throw new ApiRequestException("Something went wrong getting the notifications");
        }
    }

    /**
     * Endpoint to update notification as "read"
     *
     * @param id
     * @return
     */
    @PatchMapping("/{id}/read")
    public ResponseEntity<Notification> markAsRead(@PathVariable UUID id) {
        try {
            return ResponseEntity.ok(notificationService.markAsRead(id));
        } catch (EntityNotFoundException e) {
            throw new ApiRequestException("This notification does not exist");
        } catch (Exception e) {
            throw new ApiRequestException("Something went wrong when updating the notification");
        }
    }

    /**
     * Endpoint to update notification as "unread"
     *
     * @param id
     * @return
     */
    @PatchMapping("/{id}/unread")
    public ResponseEntity<Notification> markAsUnRead(@PathVariable UUID id) {
        try {
            return ResponseEntity.ok(notificationService.markAsUnread(id));
        } catch (EntityNotFoundException e) {
            throw new ApiRequestException("This notification does not exist");
        } catch (Exception e) {
            throw new ApiRequestException("Something went wrong when updating the notification");
        }
    }

}
