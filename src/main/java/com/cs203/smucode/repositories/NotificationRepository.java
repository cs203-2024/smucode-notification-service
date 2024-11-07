package com.cs203.smucode.repositories;

import com.cs203.smucode.models.Notification;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface NotificationRepository
    extends JpaRepository<Notification, UUID> {
    @Query(
        "SELECT n FROM Notification n WHERE :username MEMBER OF n.recipients"
    )
    List<Notification> findByUsername(String username);

    @Query(
        "SELECT n FROM Notification n WHERE :username MEMBER OF n.recipients AND n.isRead = :isRead"
    )
    List<Notification> findByUsernameAndIsRead(String username, boolean isRead);
}
