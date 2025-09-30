package com.equip.equiprental.notification.dto;

import java.time.LocalDateTime;

public record NotificationDto(
        Long notificationId,
        String message,
        String link,
        String type,
        String status,
        Long memberId,
        String memberName,
        LocalDateTime createdAt
) {}
