package com.equip.equiprental.equipment.dto;

import com.equip.equiprental.equipment.domain.EquipmentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class EquipmentItemDto {
    private Long equipmentItemId;
    private String serialNumber;
    private EquipmentStatus status;
}
