package com.cs203.smucode.repositories;

import com.cs203.smucode.models.Notification;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NotificationRepository
    extends JpaRepository<Notification, UUID> {
    List<Notification> findByUsername(String username);
    List<Notification> findByUsernameAndIsRead(String username, boolean isRead);
}
