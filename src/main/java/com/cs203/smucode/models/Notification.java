package com.cs203.smucode.models;

import com.cs203.smucode.constants.NotificationCategory;
import com.cs203.smucode.constants.NotificationType;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "notifications")
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(nullable = false)
    private UUID tournamentId;

    @Column(nullable = false)
    private String tournamentName;

    @Column(nullable = false)
    private String message;

    @Enumerated(EnumType.STRING)
    private NotificationType type;

    @Enumerated(EnumType.STRING)
    private NotificationCategory category;

    private LocalDateTime createdAt;

    private Boolean isRead;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
            name = "notification_recipients",
            joinColumns = @JoinColumn(name = "notification_id")
    )
    @Column(name = "recipient")
    private List<String> recipients;
}
