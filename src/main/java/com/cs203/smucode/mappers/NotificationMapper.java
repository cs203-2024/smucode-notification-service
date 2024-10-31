package com.cs203.smucode.mappers;

import com.cs203.smucode.constants.NotificationType;
import com.cs203.smucode.dto.NotificationDTO;
import com.cs203.smucode.models.Notification;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper(componentModel = "spring")
public interface NotificationMapper {

    @Mapping(source = "type", target = "type", qualifiedByName = "stringToNotificationType")
    Notification NotificationDTOtoNotification(NotificationDTO notificationDTO);


    @Named("stringToNotificationType")
    default NotificationType stringToNotificationType(String type) {
        return NotificationType.valueOf(type.toUpperCase());
    }
}
