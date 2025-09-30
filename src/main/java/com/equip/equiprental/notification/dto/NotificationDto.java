package com.equip.equiprental.notification.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
@Builder
public class NotificationDto {
    Long notificationId;
    String status;
    String type;
    String message;
    String link;
    Long memberId;
    String memberName;
    LocalDateTime createdAt;
}

