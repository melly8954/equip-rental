package com.equip.equiprental.notification.repository;

import com.equip.equiprental.notification.domain.Notification;
import com.equip.equiprental.notification.domain.NotificationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    int countByMember_MemberIdAndStatus(Long memberId, NotificationStatus notificationStatus);

    @Query("""
        SELECT n
        FROM Notification n
        JOIN FETCH n.member m
        WHERE m.memberId = :memberId
         AND (:status IS NULL OR n.status = :status)
        ORDER BY n.createdAt DESC
    """)
    Page<Notification> findNotifications(@Param("status") NotificationStatus status, @Param("memberId") Long memberId, Pageable pageable);
}
