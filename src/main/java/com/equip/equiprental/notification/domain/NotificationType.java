package com.equip.equiprental.notification.domain;

public enum NotificationType {
    EQUIPMENT_OUT_OF_STOCK,      // 특정 장비 재고 0 → 관리자
    RENTAL_REQUEST,              // 사용자가 대여 신청 → 관리자
    RENTAL_APPROVED,             // 관리자가 대여 승인 → 사용자
    RENTAL_REJECTED,             // 관리자가 대여 거절 → 사용자
    RENTAL_DUE_TOMORROW,         // 반납 예정일 하루 전 → 사용자
    RENTAL_OVERDUE,              // 사용자의 대여 연체 → 사용자 + 관리자
    RENTAL_RETURNED,             // 관리자가 반납 처리 → 사용자
    SYSTEM_ANNOUNCEMENT,        // 관리자 공지사항 등록 → 모든 사용자
    SUGGESTION_CREATED,          // 사용자 문의 글 등록 → 관리자
    SUGGESTION_ANSWERED,         // 문의 글 답변
}
