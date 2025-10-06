package com.equip.equiprental.notification.service.iface;

import com.equip.equiprental.common.dto.PageResponseDto;
import com.equip.equiprental.equipment.domain.Category;
import com.equip.equiprental.member.domain.Member;
import com.equip.equiprental.notification.domain.NotificationType;
import com.equip.equiprental.notification.dto.NotificationDto;
import com.equip.equiprental.notification.dto.NotificationFilter;
import com.equip.equiprental.notification.dto.ReadRequestDto;
import com.equip.equiprental.notification.dto.UnreadCountResponseDto;

public interface NotificationService {
    void createNotification(Member member, NotificationType type, String message, String link);
    void notifyManagersAndAdmins(Category category, NotificationType type, String message, String link);

    UnreadCountResponseDto getUnreadCount(Long memberId);

    PageResponseDto<NotificationDto> getNotificationList(NotificationFilter paramDto, Long memberId);

    void updateNotificationStatus(Long notificationId, ReadRequestDto dto);
}
