package com.equip.equiprental.dashboard.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
@Builder
public class KpiResponseDto {
    private List<KpiItemDto> kpis;
}
