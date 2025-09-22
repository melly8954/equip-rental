package com.equip.equiprental.scope.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class ManagerScopeRequest {
    private Long managerId;
    private Long categoryId;
}
