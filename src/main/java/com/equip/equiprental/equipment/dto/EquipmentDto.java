package com.equip.equiprental.equipment.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class EquipmentDto {
    private Long equipmentId;
    private String category;
    private String subCategory;
    private String model;
    private Integer availableStock; // 사용 가능한 재고
    private Integer totalStock;     // 전체 재고
    private String imageUrl;
}
