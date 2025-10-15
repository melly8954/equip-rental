package com.equip.equiprental.dashboard.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class InventoryDetail {
    private Long equipmentId;
    private String model;
    private Integer totalStock;
    private Integer availableCount;
}
