package com.equip.equiprental.equipment.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class EquipmentDto {
    private Long equipmentId;
    private String category;
    private String subCategory;
    private String model;
    private Integer stock;
    private String imageUrl;
}
