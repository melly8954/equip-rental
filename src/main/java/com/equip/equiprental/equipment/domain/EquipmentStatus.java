package com.equip.equiprental.equipment.domain;

public enum EquipmentStatus {
    AVAILABLE,      // 사용 가능
    RENTED,         // 대여 중
    REPAIRING,      // 수리 중
    OUT_OF_STOCK,   // 재고 없음
    LOST           // 분실
}
