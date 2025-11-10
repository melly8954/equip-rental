package com.equip.equiprental.notification.service;

import com.equip.equiprental.common.dto.PageResponseDto;
import com.equip.equiprental.common.exception.CustomException;
import com.equip.equiprental.common.exception.ErrorType;
import com.equip.equiprental.equipment.domain.Category;
import com.equip.equiprental.member.domain.Member;
import com.equip.equiprental.member.domain.MemberRole;
import com.equip.equiprental.member.repository.MemberRepository;
import com.equip.equiprental.notification.domain.Notification;
import com.equip.equiprental.notification.domain.NotificationStatus;
import com.equip.equiprental.notification.domain.NotificationType;
import com.equip.equiprental.notification.dto.NotificationDto;
import com.equip.equiprental.notification.dto.NotificationFilter;
import com.equip.equiprental.notification.dto.ReadRequestDto;
import com.equip.equiprental.notification.dto.UnreadCountResponseDto;
import com.equip.equiprental.notification.repository.NotificationRepository;
import com.equip.equiprental.notification.service.iface.NotificationService;
import com.equip.equiprental.scope.repository.ManagerScopeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
    public void notifyManagersAndAdmins(Category category, NotificationType type, String message, String link) {
        // 카테고리 매니저 조회
        List<Member> managers = managerScopeRepository.findManagersByCategory(category);

        // ADMIN 포함
        List<Member> admins = memberRepository.findByRole(MemberRole.ADMIN);
        managers.addAll(admins);

        // 중복 제거(Optional)
        Set<Long> notifiedIds = new HashSet<>();
        for (Member manager : managers) {
            if (notifiedIds.add(manager.getMemberId())) { // 중복 방지
                createNotification(manager, type, message, link);
            }
        }
    }

    @Override
    @Transactional(readOnly = true)
    public UnreadCountResponseDto getUnreadCount(Long memberId) {
        int unreadCount = notificationRepository.countByMember_MemberIdAndStatus(memberId, NotificationStatus.UNREAD);

        return new UnreadCountResponseDto(unreadCount);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponseDto<NotificationDto> getNotificationList(NotificationFilter paramDto, Long memberId) {
        Pageable pageable = paramDto.getPageable();

        Page<Notification> page = notificationRepository.findNotifications(paramDto.getStatus(), memberId, pageable);

        return PageResponseDto.<NotificationDto>builder()
                .content(page.stream()
                        .map(n -> NotificationDto.builder()
                                .notificationId(n.getNotificationId())
                                .status(n.getStatus().name())
                                .type(n.getType().name())
                                .message(n.getMessage())
                                .link(n.getLink())
                                .memberId(n.getMember().getMemberId())
                                .memberName(n.getMember().getUsername())
                                .createdAt(n.getCreatedAt())
                                .build())
                        .toList())
                .page(page.getNumber() + 1)
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .numberOfElements(page.getNumberOfElements())
                .first(page.isFirst())
                .last(page.isLast())
                .empty(page.isEmpty())
                .build();
    }

    @Override
    @Transactional
    public void updateNotificationStatus(Long notificationId, ReadRequestDto dto) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new CustomException(ErrorType.NOT_FOUND, "알림 정보를 찾을 수 업습니다."));

        notification.markAsRead(dto);
    }
}
