package com.cs203.smucode.handlers;

import com.cs203.smucode.models.Notification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class EventHandler {

    private static final Logger logger = LoggerFactory.getLogger(EventHandler.class);
    private final Map<String, SseEmitter> emitters = new ConcurrentHashMap<>();

    public SseEmitter subscribe(String username) {
        SseEmitter emitter = new SseEmitter(Long.MAX_VALUE); // Keep connection open indefinitely
        emitters.put(username, emitter);

        // Remove emitter on completion or timeout
        emitter.onCompletion(() -> emitters.remove(username));
        emitter.onTimeout(() -> emitters.remove(username));

        return emitter;
    }

    public void sendNotification(String username, String message) {
        SseEmitter emitter = emitters.get(username);

        if (emitter == null) {
            return;
        }

        try {
            // Send message as an SSE event
            emitter.send(SseEmitter.event().data(message));
        } catch (IOException e) {
            // Remove emitter if there's an error - eg. client disconnecting
            emitter.completeWithError(e);
        }
    }

    public void handleEvent(Notification notification) {
        logger.info("Received event: {}", notification);

        // Notify users about the tournament start
        for (String user : emitters.keySet()) {
            sendNotification(
                    user,
                    notification.toString()
            );
        }
    }
}
