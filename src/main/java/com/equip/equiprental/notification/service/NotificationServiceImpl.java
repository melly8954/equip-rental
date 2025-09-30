package com.equip.equiprental.notification.service;

import com.equip.equiprental.common.exception.CustomException;
import com.equip.equiprental.common.exception.ErrorType;
import com.equip.equiprental.equipment.domain.Equipment;
import com.equip.equiprental.member.domain.Member;
import com.equip.equiprental.member.domain.MemberRole;
import com.equip.equiprental.member.repository.MemberRepository;
import com.equip.equiprental.notification.domain.Notification;
import com.equip.equiprental.notification.domain.NotificationStatus;
import com.equip.equiprental.notification.domain.NotificationType;
import com.equip.equiprental.notification.dto.NotificationDto;
import com.equip.equiprental.notification.dto.UnreadCountResponseDto;
import com.equip.equiprental.notification.repository.NotificationRepository;
import com.equip.equiprental.notification.service.iface.NotificationService;
import com.equip.equiprental.rental.domain.Rental;
import com.equip.equiprental.scope.repository.ManagerScopeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {
    private final NotificationRepository notificationRepository;
    private final MemberRepository memberRepository;
    private final ManagerScopeRepository managerScopeRepository;

    @Override
    @Transactional
    public void createNotification(Member member, NotificationType type, String message, String link) {
        Notification notification = Notification.builder()
                .member(member)
                .type(type)
                .message(message)
                .link(link)
                .status(NotificationStatus.UNREAD)
                .build();

        notificationRepository.save(notification);
    }

    @Override
    @Transactional
    public void handleRentalRequest(Rental rental) {
        Equipment equipment = rental.getEquipment();

        // 카테고리 기반 매니저 조회
        List<Member> managers = managerScopeRepository.findManagersByCategory(equipment.getSubCategory().getCategory());

        // ADMIN 포함
        List<Member> admins = memberRepository.findByRole(MemberRole.ADMIN);
        managers.addAll(admins);

        // 각 관리자에게 알림 생성 + Redis publish
        for (Member manager : managers) {
            createNotification(
                    manager,
                    NotificationType.RENTAL_REQUEST,
                    rental.getMember().getName() + "님이 장비 '" + equipment.getModel() + "' 대여 신청",
                    null
            );
        }
    }

    @Override
    @Transactional
    public List<NotificationDto> getUnreadNotifications(Long memberId) {
        List<Notification> list = notificationRepository.findByMember_MemberIdAndStatus(memberId, NotificationStatus.UNREAD);

        return list.stream()
                .map(n -> new NotificationDto(
                        n.getNotificationId(),
                        n.getMessage(),
                        n.getLink(),
                        n.getType().name(),
                        n.getStatus().name(),
                        n.getMember() != null ? n.getMember().getMemberId() : null,
                        n.getMember() != null ? n.getMember().getName() : null,
                        n.getCreatedAt()
                ))
                .toList();
    }

    @Override
    @Transactional
    public UnreadCountResponseDto getUnreadCount(Long memberId) {
        int unreadCount = notificationRepository.countByMember_MemberIdAndStatus(memberId, NotificationStatus.UNREAD);

        return new UnreadCountResponseDto(unreadCount);
    }
}
