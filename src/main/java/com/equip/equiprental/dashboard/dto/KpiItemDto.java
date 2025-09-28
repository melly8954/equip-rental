package com.equip.equiprental.dashboard.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class KpiItemDto {
    private String name;      // KPI 이름
    private int value;        // KPI 값
    private String changeRate; // 전월 대비 증감률
}