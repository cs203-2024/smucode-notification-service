package com.cs203.smucode.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record IncomingNotificationDTO(
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
    boolean isRead,

    @NotEmpty(message = "Recipients cannot be empty")
    List<String> recipients

) {}
