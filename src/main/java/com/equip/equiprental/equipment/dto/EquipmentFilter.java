package com.equip.equiprental.equipment.dto;


import com.equip.equiprental.common.dto.SearchParamDto;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class EquipmentFilter extends SearchParamDto {
    private Long categoryId;
    private Long subCategoryId;
    private String model;
}
