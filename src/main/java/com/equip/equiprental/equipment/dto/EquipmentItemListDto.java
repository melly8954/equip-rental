package com.equip.equiprental.equipment.dto;

import com.equip.equiprental.common.dto.PageResponseDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class EquipmentItemListDto {
    private EquipmentDto equipmentSummary;
    private PageResponseDto<EquipmentItemDto> equipmentItems;

}
