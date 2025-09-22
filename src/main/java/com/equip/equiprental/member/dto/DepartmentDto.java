package com.equip.equiprental.member.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class DepartmentDto {
    private Long departmentId;
    private String departmentName;
}
