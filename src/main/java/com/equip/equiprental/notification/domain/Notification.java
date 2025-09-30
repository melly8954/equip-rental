package com.equip.equiprental.notification.domain;

import com.equip.equiprental.common.domain.BaseEntity;
import com.equip.equiprental.member.domain.Member;
import com.equip.equiprental.notification.dto.ReadRequestDto;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "notification_tbl")
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Notification extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="notification_id")
    private Long notificationId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member; // nullable, 전체 대상일 때 null 가능

    @Enumerated(EnumType.STRING)
    private NotificationType type;

    private String message;
    private String link;

    @Enumerated(EnumType.STRING)
    private NotificationStatus status;

    public void markAsRead(ReadRequestDto dto) {
        if (dto.getNotificationStatus() != null) {
            this.status = dto.getNotificationStatus();
        }
    }
}
