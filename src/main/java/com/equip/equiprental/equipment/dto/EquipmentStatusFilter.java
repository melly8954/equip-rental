package com.equip.equiprental.equipment.dto;


import com.equip.equiprental.common.dto.SearchParamDto;
import com.equip.equiprental.equipment.domain.EquipmentStatus;
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
public class EquipmentStatusFilter extends SearchParamDto {
    private EquipmentStatus status;
}
