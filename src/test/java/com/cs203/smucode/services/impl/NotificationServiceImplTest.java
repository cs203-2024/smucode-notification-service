package com.cs203.smucode.services.impl;

import com.cs203.smucode.models.Notification;
import com.cs203.smucode.repositories.NotificationRepository;
import com.cs203.smucode.constants.NotificationCategory;
import com.cs203.smucode.constants.NotificationType;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceImplTest {

    @Mock
    private NotificationRepository notificationRepository;

    private NotificationServiceImpl notificationService;

    private Notification testNotification;
    private final UUID testId = UUID.randomUUID();
    private final UUID testTournamentId = UUID.randomUUID();
    private final String testTournamentName = "Test Tournament";
    private final List<String> testRecipients = Arrays.asList("user1", "user2");

    @BeforeEach
    void setUp() {
        notificationService = new NotificationServiceImpl(notificationRepository);

        testNotification = new Notification();
        testNotification.setId(testId);
        testNotification.setTournamentId(testTournamentId);
        testNotification.setTournamentName(testTournamentName);
        testNotification.setMessage("Test message");
        testNotification.setType(NotificationType.TOURNAMENT_STARTED);
        testNotification.setCategory(NotificationCategory.ALERT);
        testNotification.setCreatedAt(LocalDateTime.now());
        testNotification.setIsRead(false);
        testNotification.setRecipients(testRecipients);
    }

    @Test
    void createNotification_ShouldSetDefaultValuesAndSave() {
        when(notificationRepository.save(any(Notification.class))).thenReturn(testNotification);

        Notification newNotification = new Notification();
        newNotification.setTournamentId(testTournamentId);
        newNotification.setTournamentName(testTournamentName);
        newNotification.setMessage("Test message");
        newNotification.setType(NotificationType.TOURNAMENT_STARTED);
        newNotification.setCategory(NotificationCategory.ALERT);
        newNotification.setRecipients(testRecipients);

        Notification result = notificationService.createNotification(newNotification);

        assertNotNull(result);
        assertNotNull(result.getCreatedAt());
        assertFalse(result.getIsRead());
        assertEquals(testTournamentId, result.getTournamentId());
        assertEquals(testTournamentName, result.getTournamentName());
        assertEquals(NotificationType.TOURNAMENT_STARTED, result.getType());
        assertEquals(NotificationCategory.ALERT, result.getCategory());
        assertEquals(testRecipients, result.getRecipients());
        verify(notificationRepository).save(any(Notification.class));
    }

    @Test
    void getNotificationsByUsername_ShouldReturnListOfNotifications() {
        String username = "user1";
        List<Notification> expectedNotifications = Arrays.asList(testNotification);
        when(notificationRepository.findByUsername(username)).thenReturn(expectedNotifications);

        List<Notification> result = notificationService.getNotificationsByUsername(username);

        assertNotNull(result);
        assertEquals(1, result.size());
        Notification notification = result.get(0);
        assertEquals(testId, notification.getId());
        assertEquals(testTournamentId, notification.getTournamentId());
        assertEquals(testTournamentName, notification.getTournamentName());
        assertEquals(NotificationType.TOURNAMENT_STARTED, notification.getType());
        assertEquals(NotificationCategory.ALERT, notification.getCategory());
        assertTrue(notification.getRecipients().contains(username));
        verify(notificationRepository).findByUsername(username);
    }

    @Test
    void getUnreadNotificationsByUsername_ShouldReturnUnreadNotifications() {
        String username = "user1";
        List<Notification> expectedNotifications = Arrays.asList(testNotification);
        when(notificationRepository.findByUsernameAndIsRead(username, false))
                .thenReturn(expectedNotifications);

        List<Notification> result = notificationService.getUnreadNotificationsByUsername(username);

        assertNotNull(result);
        assertEquals(1, result.size());
        Notification notification = result.get(0);
        assertFalse(notification.getIsRead());
        assertEquals(testId, notification.getId());
        assertEquals(testTournamentId, notification.getTournamentId());
        assertEquals(NotificationType.TOURNAMENT_STARTED, notification.getType());
        assertTrue(notification.getRecipients().contains(username));
        verify(notificationRepository).findByUsernameAndIsRead(username, false);
    }

    @Test
    void markAsRead_ShouldUpdateAndReturnNotification() {
        when(notificationRepository.findById(testId)).thenReturn(Optional.of(testNotification));
        testNotification.setIsRead(true);
        when(notificationRepository.save(any(Notification.class))).thenReturn(testNotification);

        Notification result = notificationService.markAsRead(testId);

        assertNotNull(result);
        assertTrue(result.getIsRead());
        assertEquals(testId, result.getId());
        assertEquals(testTournamentId, result.getTournamentId());
        assertEquals(testTournamentName, result.getTournamentName());
        assertEquals(NotificationType.TOURNAMENT_STARTED, result.getType());
        assertEquals(NotificationCategory.ALERT, result.getCategory());
        assertEquals(testRecipients, result.getRecipients());
        verify(notificationRepository).findById(testId);
        verify(notificationRepository).save(any(Notification.class));
    }

    @Test
    void markAsRead_ShouldThrowEntityNotFoundExceptionWhenNotificationDoesNotExist() {
        when(notificationRepository.findById(testId)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> notificationService.markAsRead(testId));
        verify(notificationRepository).findById(testId);
        verify(notificationRepository, never()).save(any(Notification.class));
    }

    @Test
    void markAsUnread_ShouldUpdateAndReturnNotification() {
        testNotification.setIsRead(true);
        when(notificationRepository.findById(testId)).thenReturn(Optional.of(testNotification));
        testNotification.setIsRead(false);
        when(notificationRepository.save(any(Notification.class))).thenReturn(testNotification);

        Notification result = notificationService.markAsUnread(testId);

        assertNotNull(result);
        assertFalse(result.getIsRead());
        assertEquals(testId, result.getId());
        assertEquals(testTournamentId, result.getTournamentId());
        assertEquals(testTournamentName, result.getTournamentName());
        assertEquals(NotificationType.TOURNAMENT_STARTED, result.getType());
        assertEquals(NotificationCategory.ALERT, result.getCategory());
        assertEquals(testRecipients, result.getRecipients());
        verify(notificationRepository).findById(testId);
        verify(notificationRepository).save(any(Notification.class));
    }

    @Test
    void markAsUnread_ShouldThrowEntityNotFoundExceptionWhenNotificationDoesNotExist() {
        when(notificationRepository.findById(testId)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> notificationService.markAsUnread(testId));
        verify(notificationRepository).findById(testId);
        verify(notificationRepository, never()).save(any(Notification.class));
    }

    @Test
    void subscribe_ShouldReturnSseEmitterAndAddToEmitters() {
        String username = "user1";
        SseEmitter result = notificationService.subscribe(username);

        assertNotNull(result);
        assertTrue(notificationService.getEmitters().containsKey(username));
        assertEquals(result, notificationService.getEmitters().get(username));
    }

    @Test
    void subscribe_ShouldReplaceExistingEmitterForSameUser() {
        String username = "user1";
        SseEmitter firstEmitter = notificationService.subscribe(username);
        SseEmitter secondEmitter = notificationService.subscribe(username);

        assertNotEquals(firstEmitter, secondEmitter);
        assertEquals(secondEmitter, notificationService.getEmitters().get(username));
        assertEquals(1, notificationService.getEmitters().size());
    }

    @Test
    void subscribe_EmitterError_RemovesEmitterAndCompletes() {
        // Arrange
        NotificationRepository mockRepository = mock(NotificationRepository.class);
        NotificationServiceImpl mockNotiService = new NotificationServiceImpl(mockRepository);
        String testUsername = "testUser";

        // Act
        SseEmitter emitter = mockNotiService.subscribe(testUsername);
        Consumer<Throwable> errorHandler = (Consumer<Throwable>) ReflectionTestUtils.getField(emitter, "errorCallback");

        // Verify emitter was added
        assertThat(mockNotiService.getEmitters()).containsKey(testUsername);
        errorHandler.accept(new IOException("Test Error"));

        // Assert
        assertThat(mockNotiService.getEmitters()).doesNotContainKey(testUsername);

        // Verify emitter is completed
        assertThatThrownBy(() -> emitter.send("test"))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void subscribe_EmitterComplete_RemovesEmitter() {
        // Arrange
        NotificationRepository mockRepository = mock(NotificationRepository.class);
        NotificationServiceImpl mockNotiService = new NotificationServiceImpl(mockRepository);
        String testUsername = "testUser";

        // Act
        SseEmitter emitter = mockNotiService.subscribe(testUsername);
        Runnable completionHandler = (Runnable) ReflectionTestUtils.getField(emitter, "completionCallback");

        // Verify emitter was added
        assertThat(mockNotiService.getEmitters()).containsKey(testUsername);

        completionHandler.run();

        // Assert
        assertThat(mockNotiService.getEmitters()).doesNotContainKey(testUsername);

        // Verify emitter is completed
        assertThatThrownBy(() -> emitter.send("test"))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void subscribe_EmitterTimeout_RemovesEmitter() {
        // Arrange
        NotificationRepository mockRepository = mock(NotificationRepository.class);
        NotificationServiceImpl mockNotiService = new NotificationServiceImpl(mockRepository);
        String testUsername = "testUser";

        // Act
        SseEmitter emitter = mockNotiService.subscribe(testUsername);
        Runnable timeoutHandler = (Runnable) ReflectionTestUtils.getField(emitter, "timeoutCallback");

        // Verify emitter was added
        assertThat(mockNotiService.getEmitters()).containsKey(testUsername);

        timeoutHandler.run();

        // Assert
        assertThat(mockNotiService.getEmitters()).doesNotContainKey(testUsername);

        // Verify emitter is completed
        assertThatThrownBy(() -> emitter.send("test"))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void getEmitters_ShouldReturnCurrentEmitters() {
        String username1 = "user1";
        String username2 = "user2";

        SseEmitter emitter1 = notificationService.subscribe(username1);
        SseEmitter emitter2 = notificationService.subscribe(username2);

        var emitters = notificationService.getEmitters();

        assertEquals(2, emitters.size());
        assertEquals(emitter1, emitters.get(username1));
        assertEquals(emitter2, emitters.get(username2));
    }
}