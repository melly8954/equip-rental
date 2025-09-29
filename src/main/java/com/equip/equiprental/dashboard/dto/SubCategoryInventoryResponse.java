package com.equip.equiprental.dashboard.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class SubCategoryInventoryResponse {
    private Long subCategoryId;
    private String subCategoryLabel;
    private int stock;
}
