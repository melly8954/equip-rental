package com.equip.equiprental.notification.repository;

import com.equip.equiprental.notification.domain.Notification;
import com.equip.equiprental.notification.domain.NotificationStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByMember_MemberIdAndStatus(Long memberId, NotificationStatus notificationStatus);
}
