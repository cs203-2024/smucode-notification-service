package com.cs203.smucode.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record OutgoingNotificationDTO(
    @NotNull(message = "Tournament ID cannot be null")
    UUID tournamentId,

    @NotNull(message = "Tournament name cannot be null")
    @NotBlank(message = "Tournament name cannot be blank")
    String tournamentName,

    @NotNull(message = "Message cannot be null")
    @NotBlank(message = "Message cannot be blank")
    String message,

    @NotNull(message = "Notification type cannot be null")
    String type
) {}
