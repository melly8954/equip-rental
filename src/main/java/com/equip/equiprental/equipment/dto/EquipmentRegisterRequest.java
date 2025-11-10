package com.equip.equiprental.equipment.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class EquipmentRegisterRequest {
    private Long subCategoryId;
    private String model;
    private Integer stock;
}
