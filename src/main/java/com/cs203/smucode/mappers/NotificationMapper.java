package com.cs203.smucode.mappers;

import com.cs203.smucode.constants.NotificationType;
import com.cs203.smucode.dto.IncomingNotificationDTO;
import com.cs203.smucode.dto.OutgoingNotificationDTO;
import com.cs203.smucode.models.Notification;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.List;

@Mapper(componentModel = "spring")
public interface NotificationMapper {

    @Mapping(source = "type", target = "type", qualifiedByName = "stringToNotificationType")
    Notification incomingNotificationDTOtoNotification(IncomingNotificationDTO notificationDTO);

    @Mapping(source = "type", target = "type", qualifiedByName = "notificationTypeToString")
    OutgoingNotificationDTO notificationToOutgoingNotificationDTO(Notification notification);

    List<OutgoingNotificationDTO> notificationsToOutgoingNotificationDTOs(List<Notification> notifications);

    @Named("stringToNotificationType")
    default NotificationType stringToNotificationType(String type) {
        return NotificationType.valueOf(type.toUpperCase());
    }

    @Named("notificationTypeToString")
    default String notificationTypeToString(NotificationType type) {
        return type.toString().toLowerCase();
    }
}
