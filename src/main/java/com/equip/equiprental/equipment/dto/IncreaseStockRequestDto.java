package com.equip.equiprental.equipment.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class IncreaseStockRequestDto {
    private int amount;
}
