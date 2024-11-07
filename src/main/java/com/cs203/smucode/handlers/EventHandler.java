package com.cs203.smucode.handlers;

import com.cs203.smucode.constants.NotificationType;
import com.cs203.smucode.dto.OutgoingNotificationDTO;
import com.cs203.smucode.models.Notification;
import com.cs203.smucode.services.INotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@Service
public class EventHandler {

    private static final Logger logger = LoggerFactory.getLogger(EventHandler.class);
    private final INotificationService notificationService;

    @Autowired
    public EventHandler(INotificationService notificationService) {
        this.notificationService = notificationService;
    }

    /**
     * Method to handle incoming event
     *
     * @param notification incoming notification
     */
    public void handleEvent(List<String> recipients, OutgoingNotificationDTO notification) {
        logger.info("Received event: {}", notification);

        // Notify recipients about the tournament start
        for (String recipient : recipients) {
            logger.info("Sending event: {} to user: {}", notification, recipient);
            sendNotification(
                    recipient,
                    notification.toString()
            );
        }
    }

    /**
     * Method to send notification to relevant subscribed users
     *
     * @param username subscriber to send notification to
     * @param message notification to be sent
     */
    public void sendNotification(String username,
                                 String message) {
        Map<String, SseEmitter> emitters = notificationService.getEmitters();
        SseEmitter emitter = emitters.get(username);

        if (emitter == null) {
            logger.info("User: {} does not have associated emitter", username);
            return;
        }

        try {
            // Send message as an SSE event
            emitter.send(SseEmitter.event().data(message));
        } catch (IOException e) { // If there's an error - eg. client disconnecting
            emitter.completeWithError(e); // Marks the SseEmitter as completed due to an error - removes emitter
        }
    }
}
