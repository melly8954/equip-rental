package com.equip.equiprental.notification.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class UnreadCountResponseDto {
    private int unreadCount;
}
