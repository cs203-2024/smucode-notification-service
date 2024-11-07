package com.cs203.smucode.controllers;

import com.cs203.smucode.dto.IncomingNotificationDTO;
import com.cs203.smucode.dto.OutgoingNotificationDTO;
import com.cs203.smucode.exception.ApiRequestException;
import com.cs203.smucode.exception.InvalidTokenException;
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
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
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
    public ResponseEntity<OutgoingNotificationDTO> createNotification(
        @RequestBody @Valid IncomingNotificationDTO notificationDTO
    ) {
        try {
            Notification notification = notificationMapper.incomingNotificationDTOtoNotification(notificationDTO);
            notificationService.createNotification(notification);

            logger.info("Notification created: {}", notification);

            // Handle incoming event, eg. notify subscribed users
            OutgoingNotificationDTO outgoingNotificationDTO = notificationMapper.notificationToOutgoingNotificationDTO(notification);
            eventHandler.handleEvent(notificationDTO.recipients(), outgoingNotificationDTO);
            return ResponseEntity.ok(outgoingNotificationDTO);

        } catch (IllegalArgumentException e) {
            throw new ApiRequestException("Invalid notification type");
        } catch (Exception e) {
            logger.error("Exception during notification creation", e);
            throw new ApiRequestException("Something went wrong creating a notification");
        }
    }

    /**
     * Endpoint to subscribe user to notification service
     *
     * @param jwt the jwt token containing the subject
     * @return SseEmitter which keeps the connection open and streams incoming notifications
     */
    @GetMapping(path ="/subscribe", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter subscribe(@AuthenticationPrincipal Jwt jwt) {
        try {
            validateJwt(jwt);
            logger.info("Subscribing");
            return notificationService.subscribe(this.extractUsername(jwt));
        } catch (InvalidTokenException e) {
            throw new InvalidTokenException("Invalid token");
        } catch (ApiRequestException e) {
            throw e;
        } catch (Exception e) {
            throw new ApiRequestException("Something went wrong subscribing to a notification");
        }
    }

    /**
     * Endpoint to get notifications for user
     *
     * @param jwt the jwt token containing the subject
     * @return list of all notifications (read + unread) for the user
     */
    @GetMapping("/")
    public ResponseEntity<List<OutgoingNotificationDTO>> getNotificationsByUsername(@AuthenticationPrincipal Jwt jwt) {
        try {
            validateJwt(jwt);
            List<Notification> notifications = notificationService.getNotificationsByUsername(
                    this.extractUsername(jwt)
            );
            List<OutgoingNotificationDTO> notificationDTOs =
                    notificationMapper.notificationsToOutgoingNotificationDTOs(notifications);
            return ResponseEntity.ok(notificationDTOs);
        } catch (Exception e) {
            logger.error("Exception during getNotificationsByUsername", e);
            throw new ApiRequestException("Something went wrong getting the notifications");
        }
    }

    /**
     * Endpoint to get all unread notifications for user
     *
     * @param jwt the jwt token containing the subject
     * @return list of all notifications (unread) for the user
     */
    @GetMapping("/unread")
    public ResponseEntity<List<OutgoingNotificationDTO>> getUnreadNotificationsByUsername(
        @AuthenticationPrincipal Jwt jwt
    ) {
        try {
            List<Notification> notifications = notificationService.getUnreadNotificationsByUsername(
                    this.extractUsername(jwt)
            );
            List<OutgoingNotificationDTO> notificationDTOs =
                    notificationMapper.notificationsToOutgoingNotificationDTOs(notifications);
            return ResponseEntity.ok(notificationDTOs);
        } catch (Exception e) {
            logger.error("Exception during getUnreadNotificationsByUsername", e);
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
    public ResponseEntity<OutgoingNotificationDTO> markAsRead(@PathVariable UUID id) {
        try {
            Notification notification = notificationService.markAsRead(id);
            OutgoingNotificationDTO notificationDTO =
                    notificationMapper.notificationToOutgoingNotificationDTO(notification);
            return ResponseEntity.ok(notificationDTO);
        } catch (EntityNotFoundException e) {
            throw new ApiRequestException("This notification does not exist");
        } catch (Exception e) {
            logger.error("Exception during markAsRead", e);
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
    public ResponseEntity<OutgoingNotificationDTO> markAsUnRead(@PathVariable UUID id) {
        try {
            Notification notification = notificationService.markAsUnread(id);
            OutgoingNotificationDTO notificationDTO =
                    notificationMapper.notificationToOutgoingNotificationDTO(notification);
            return ResponseEntity.ok(notificationDTO);
        } catch (EntityNotFoundException e) {
            throw new ApiRequestException("This notification does not exist");
        } catch (Exception e) {
            logger.error("Exception during markAsUnRead", e);
            throw new ApiRequestException("Something went wrong when updating the notification");
        }
    }


    private void validateJwt(@AuthenticationPrincipal Jwt jwt) {
        if (jwt == null) {
            throw new InvalidTokenException("Invalid token");
        }
    }

    private String extractUsername(@AuthenticationPrincipal Jwt jwt) {
        return jwt.getSubject();
    }
}
