package com.equip.equiprental.notification.dto;

import com.equip.equiprental.notification.domain.NotificationStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class ReadRequestDto {
    private NotificationStatus notificationStatus;
}
