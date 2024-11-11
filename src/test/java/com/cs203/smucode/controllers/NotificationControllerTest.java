package com.cs203.smucode.controllers;

import com.cs203.smucode.config.TestSecurityConfiguration;
import com.cs203.smucode.constants.NotificationCategory;
import com.cs203.smucode.constants.NotificationType;
import com.cs203.smucode.dto.IncomingNotificationDTO;
import com.cs203.smucode.mappers.NotificationMapper;
import com.cs203.smucode.models.Notification;
import com.cs203.smucode.repositories.NotificationRepository;
import com.cs203.smucode.services.INotificationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(TestSecurityConfiguration.class)
class NotificationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private INotificationService notificationService;

    @Autowired
    private NotificationMapper notificationMapper;

    @Value("${feign.access.token}")
    private String testJWT;

    private IncomingNotificationDTO testIncomingNotificationDTO;
    private Notification testNotification;
    private final UUID testTournamentId = UUID.randomUUID();
    private final UUID testNotificationId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        notificationRepository.deleteAll();

        // Setup test notification DTO
        testIncomingNotificationDTO = new IncomingNotificationDTO(
                testTournamentId,
                "Test Tournament",
                "Tournament is starting soon!",
                "TOURNAMENT_START",
                "ALERT",
                Arrays.asList("user1", "user2")
        );

        // Setup test notification entity
        testNotification = new Notification();
        testNotification.setId(testNotificationId);
        testNotification.setTournamentId(testTournamentId);
        testNotification.setTournamentName("Test Tournament");
        testNotification.setMessage("Tournament is starting soon!");
        testNotification.setType(NotificationType.TOURNAMENT_STARTED);
        testNotification.setCategory(NotificationCategory.ALERT);
        testNotification.setRecipients(Arrays.asList("user1", "user2"));
        testNotification.setIsRead(false);
        testNotification.setCreatedAt(LocalDateTime.now());
    }

    @AfterEach
    void tearDown() {
        notificationRepository.deleteAll();
    }

    @Nested
    @DisplayName("Notification Creation Operations")
    class NotificationCreationOperations {

        @Test
        @DisplayName("Should create notification successfully")
        void createNotification_ValidData_Success() throws Exception {
            mockMvc.perform(post("/notifications/stream")  // Added leading forward slash
                            .header("Authorization", "Bearer " + testJWT)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(testIncomingNotificationDTO)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.tournamentId").value(testTournamentId.toString()))
                    .andExpect(jsonPath("$.tournamentName").value("Test Tournament"))
                    .andExpect(jsonPath("$.message").value("Tournament is starting soon!"))
                    .andExpect(jsonPath("$.type").value("tournament_started"))
                    .andExpect(jsonPath("$.category").value("alert"))
                    .andExpect(jsonPath("$.isRead").value(false));

            // Verify notification was saved
            assertThat(notificationRepository.findAll()).hasSize(1);
        }

        @Test
        @DisplayName("Should reject invalid notification type")
        void createNotification_InvalidType_ReturnsBadRequest() throws Exception {
            IncomingNotificationDTO invalidDTO = new IncomingNotificationDTO(
                    testTournamentId,
                    "Test Tournament",
                    "Test message",
                    "INVALID_TYPE",
                    "ALERT",
                    Arrays.asList("user1", "user2")
            );

            mockMvc.perform(post("/notifications/stream")
                            .header("Authorization", "Bearer " + testJWT)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalidDTO)))
                    .andDo(print())
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("Subscription Operations")
    class SubscriptionOperations {

        @Test
        @DisplayName("Should subscribe successfully")
        void subscribe_ValidToken_Success() throws Exception {
            mockMvc.perform(get("/notifications/subscribe")
                            .header("Authorization", "Bearer " + testJWT))
                    .andDo(print())
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("Should reject subscription without token")
        void subscribe_NoToken_ReturnsUnauthorized() throws Exception {
            mockMvc.perform(get("/notifications/subscribe"))
                    .andDo(print())
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("Should handle missing Authorization header")
        void subscribe_MissingAuthHeader_ReturnsUnauthorized() throws Exception {
            mockMvc.perform(get("/notifications/subscribe"))
                    .andDo(print())
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    @DisplayName("Notification Retrieval Operations")
    class NotificationRetrievalOperations {

        @Test
        @DisplayName("Should get user notifications successfully")
        void getNotifications_ValidToken_Success() throws Exception {
            notificationRepository.save(testNotification);

            mockMvc.perform(get("/notifications/")
                            .header("Authorization", "Bearer " + testJWT))
                    .andDo(print())
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("Should return empty list when no notifications exist")
        void getNotifications_NoNotifications_ReturnsEmptyList() throws Exception {
            mockMvc.perform(get("/notifications/")
                            .header("Authorization", "Bearer " + testJWT))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$").isEmpty());
        }

    }

    @Nested
    @DisplayName("Notification Status Operations")
    class NotificationStatusOperations {

        @Test
        @DisplayName("Should mark notification as read successfully")
        void markAsRead_ValidId_Success() throws Exception {
            Notification savedNotification = notificationRepository.save(testNotification);
            UUID savedId = savedNotification.getId();

            mockMvc.perform(patch("/notifications/" + savedId + "/read")
                            .header("Authorization", "Bearer " + testJWT))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.isRead").value(true));

            Notification updatedNotification = notificationRepository.findById(savedId).orElseThrow();
            assertThat(updatedNotification.getIsRead()).isTrue();
        }

        @Test
        @DisplayName("Should mark notification as unread successfully")
        void markAsUnread_ValidId_Success() throws Exception {
            testNotification.setIsRead(true);
            Notification savedNotification = notificationRepository.save(testNotification);
            UUID savedId = savedNotification.getId();

            mockMvc.perform(patch("/notifications/" + savedId + "/unread")
                            .header("Authorization", "Bearer " + testJWT))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.isRead").value(false));

            Notification updatedNotification = notificationRepository.findById(savedId).orElseThrow();
            assertThat(updatedNotification.getIsRead()).isFalse();
        }

        @Test
        @DisplayName("Should handle non-existent notification")
        void markAsRead_InvalidId_ReturnsBadRequest() throws Exception {
            UUID nonExistentId = UUID.randomUUID();

            mockMvc.perform(patch("/notifications/" + nonExistentId + "/read")
                            .header("Authorization", "Bearer " + testJWT))
                    .andDo(print())
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value("This notification does not exist"));
        }

        @Test
        @DisplayName("Should handle general exception during mark as read")
        void markAsRead_GeneralException_ReturnsBadRequest() throws Exception {
            UUID invalidUuid = null;

            mockMvc.perform(patch("/notifications/" + invalidUuid + "/read")
                            .header("Authorization", "Bearer " + testJWT))
                    .andDo(print())
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should handle general exception during mark as unread")
        void markAsUnread_GeneralException_ReturnsBadRequest() throws Exception {
            UUID invalidUuid = null;

            mockMvc.perform(patch("/notifications/" + invalidUuid + "/unread")
                            .header("Authorization", "Bearer " + testJWT))
                    .andDo(print())
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should handle non-existent notification for markAsUnread")
        void markAsUnread_NonExistentId_ReturnsBadRequest() throws Exception {
            UUID nonExistentId = UUID.randomUUID();

            mockMvc.perform(patch("/notifications/" + nonExistentId + "/unread")
                            .header("Authorization", "Bearer " + testJWT))
                    .andDo(print())
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value("This notification does not exist"));
        }

    }

    @Nested
    @DisplayName("Error Handling")
    class ErrorHandling {

        @Test
        @DisplayName("Should handle unauthorized access")
        void anyEndpoint_NoToken_ReturnsUnauthorized() throws Exception {
            mockMvc.perform(get("/notifications/"))
                    .andDo(print())
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("Should handle invalid notification data")
        void createNotification_InvalidData_ReturnsBadRequest() throws Exception {
            IncomingNotificationDTO invalidDTO = new IncomingNotificationDTO(
                    null,  // Invalid: tournamentId cannot be null
                    "",    // Invalid: tournamentName cannot be blank
                    "",    // Invalid: message cannot be blank
                    "TOURNAMENT_START",
                    "ALERT",
                    Arrays.asList("user1", "user2")
            );

            mockMvc.perform(post("/notifications/stream")
                            .header("Authorization", "Bearer " + testJWT)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalidDTO)))
                    .andDo(print())
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should handle invalid token format")
        void anyEndpoint_InvalidTokenFormat_ReturnsUnauthorized() throws Exception {
            mockMvc.perform(get("/notifications/")
                            .header("Authorization", "Invalid-Format"))
                    .andDo(print())
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("Should handle missing content type")
        void createNotification_MissingContentType_ReturnsUnsupportedMediaType() throws Exception {
            mockMvc.perform(post("/notifications/stream")
                            .header("Authorization", "Bearer " + testJWT)
                            .content(objectMapper.writeValueAsString(testIncomingNotificationDTO)))
                    .andDo(print())
                    .andExpect(status().isUnsupportedMediaType());
        }

        @Test
        @DisplayName("Should handle empty recipient list")
        void createNotification_EmptyRecipients_ReturnsBadRequest() throws Exception {
            IncomingNotificationDTO emptyRecipientsDTO = new IncomingNotificationDTO(
                    testTournamentId,
                    "Test Tournament",
                    "Test message",
                    "TOURNAMENT_START",
                    "ALERT",
                    Arrays.asList()
            );

            mockMvc.perform(post("/notifications/stream")
                            .header("Authorization", "Bearer " + testJWT)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(emptyRecipientsDTO)))
                    .andDo(print())
                    .andExpect(status().isBadRequest());
        }
    }
}