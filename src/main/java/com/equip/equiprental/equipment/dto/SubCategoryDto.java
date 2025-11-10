package com.equip.equiprental.equipment.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class SubCategoryDto {
    private Long subCategoryId;
    private Long categoryId;
    private String label;
}
