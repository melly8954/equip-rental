package com.equip.equiprental.notification.service.iface;

import com.equip.equiprental.common.dto.PageResponseDto;
import com.equip.equiprental.common.dto.SearchParamDto;
import com.equip.equiprental.member.domain.Member;
import com.equip.equiprental.notification.domain.NotificationType;
import com.equip.equiprental.notification.dto.NotificationDto;
import com.equip.equiprental.notification.dto.ReadRequestDto;
import com.equip.equiprental.notification.dto.UnreadCountResponseDto;
import com.equip.equiprental.rental.domain.Rental;

public interface NotificationService {
    void createNotification(Member member, NotificationType type, String message, String link);
    void handleRentalRequest(Rental rental);

    UnreadCountResponseDto getUnreadCount(Long memberId);

    PageResponseDto<NotificationDto> getNotificationList(SearchParamDto paramDto, Long memberId);

    void updateNotificationStatus(Long notificationId, ReadRequestDto dto);
}
