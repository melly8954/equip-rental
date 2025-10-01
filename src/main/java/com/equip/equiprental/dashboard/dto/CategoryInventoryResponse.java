package com.equip.equiprental.dashboard.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class CategoryInventoryResponse {
    private Long categoryId;
    private String categoryLabel;
    private int totalStock;
    private int availableStock;
}
