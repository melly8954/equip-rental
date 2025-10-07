package com.equip.equiprental.equipment.dto;

import com.equip.equiprental.equipment.domain.EquipmentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class UpdateItemStatusDto {
    private Long equipmentItemId;
    private EquipmentStatus newStatus;
    private boolean isAdminChange; // UI에서 직접 변경인지 여부
}
