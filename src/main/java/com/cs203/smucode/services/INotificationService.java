package com.cs203.smucode.services;

import com.cs203.smucode.models.Notification;
import java.util.List;
import java.util.UUID;

public interface INotificationService {
    Notification createNotification(Notification notification);
    List<Notification> getNotificationsByUsername(String username);
    List<Notification> getUnreadNotificationsByUsername(String username);
    Notification markAsRead(UUID id);
    Notification markAsUnread(UUID id);
}
