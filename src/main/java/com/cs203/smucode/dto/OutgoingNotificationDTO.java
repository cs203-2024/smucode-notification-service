package com.cs203.smucode.dto;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.UUID;

public record OutgoingNotificationDTO(
    @NotNull(message = "Notification ID cannot be null")
    UUID id,

    @NotNull(message = "Tournament ID cannot be null")
    UUID tournamentId,

    @NotNull(message = "Tournament name cannot be null")
    @NotBlank(message = "Tournament name cannot be blank")
    String tournamentName,

    @NotNull(message = "Message cannot be null")
    @NotBlank(message = "Message cannot be blank")
    String message,

    @NotNull(message = "Notification type cannot be null")
    String type,

    @NotNull(message = "Notification category cannot be null")
    String category,

    @NotNull(message = "Notification created time cannot be null")
    LocalDateTime createdAt,

    @NotNull(message = "Notification must have read status")
    boolean isRead

    ) {
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final Logger logger = LoggerFactory.getLogger(OutgoingNotificationDTO.class);

    public String toJson() {
        try {
            return objectMapper.writeValueAsString(this); // Serialise to JSON
        } catch (Exception e) {
            logger.error("Failed to serialize notification: {}", e.getMessage(), e);
            return "{\"error\":\"Serialization failed\"}";
        }
    }
}
