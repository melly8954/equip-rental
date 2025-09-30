package com.equip.equiprental.notification.service.iface;

import com.equip.equiprental.auth.security.PrincipalDetails;
import com.equip.equiprental.member.domain.Member;
import com.equip.equiprental.notification.domain.NotificationType;
import com.equip.equiprental.notification.dto.NotificationDto;
import com.equip.equiprental.notification.dto.UnreadCountResponseDto;
import com.equip.equiprental.rental.domain.Rental;

import java.util.List;

public interface NotificationService {
    void createNotification(Member member, NotificationType type, String message, String link);
    void handleRentalRequest(Rental rental);

    List<NotificationDto> getUnreadNotifications(Long memberId);

    UnreadCountResponseDto getUnreadCount(Long memberId);
}
