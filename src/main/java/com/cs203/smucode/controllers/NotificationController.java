package com.cs203.smucode.controllers;

import com.cs203.smucode.dto.NotificationDTO;
import com.cs203.smucode.exception.ApiRequestException;
import com.cs203.smucode.mappers.NotificationMapper;
import com.cs203.smucode.models.Notification;
import com.cs203.smucode.services.INotificationService;
import java.util.List;
import java.util.UUID;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/notifications")
public class NotificationController {

    private final NotificationMapper notificationMapper;
    private final INotificationService notificationService;

    @Autowired
    public NotificationController(INotificationService notificationService,
                                  NotificationMapper notificationMapper) {
        this.notificationService = notificationService;
        this.notificationMapper = notificationMapper;
    }

    @PostMapping("/")
    public ResponseEntity<Notification> createNotification(
        @RequestBody @Valid NotificationDTO notificationDTO
    ) {

        try {
            return new ResponseEntity<>(
                    notificationService.createNotification(
                            notificationMapper.NotificationDTOtoNotification(notificationDTO)
                    ),
                    HttpStatus.CREATED
            );
        } catch (IllegalArgumentException e) {
            throw new ApiRequestException("Invalid notification type");
        } catch (Exception e) {
            throw new ApiRequestException("Something went wrong creating a notification");
        }
    }
    @GetMapping("/{username}/")
    public ResponseEntity<List<Notification>> getNotificationsByUsername(
        @PathVariable String username
    ) {
        return ResponseEntity.ok(
            notificationService.getNotificationsByUsername(username)
        );
    }

    @GetMapping("/{username}/unread")
    public ResponseEntity<List<Notification>> getUnreadNotificationsByUsername(
        @PathVariable String username
    ) {
        return ResponseEntity.ok(
            notificationService.getUnreadNotificationsByUsername(username)
        );
    }

    @PatchMapping("/{id}/read")
    public ResponseEntity<Notification> markAsRead(@PathVariable UUID id) {
        return ResponseEntity.ok(notificationService.markAsRead(id));
    }

    @PatchMapping("/{id}/unread")
    public ResponseEntity<Notification> markAsUnRead(@PathVariable UUID id) {
        return ResponseEntity.ok(notificationService.markAsUnread(id));
    }
}
