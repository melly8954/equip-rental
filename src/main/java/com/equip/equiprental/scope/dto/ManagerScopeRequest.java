package com.equip.equiprental.scope.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
@Builder
public class ManagerScopeRequest {
    private Long managerId;
    private List<Long> categoryIds;
}
