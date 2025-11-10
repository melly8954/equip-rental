package com.equip.equiprental.dashboard.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class ZeroStockDto {
    private Long equipmentId;
    private String category;
    private String subCategory;
    private String model;
}
