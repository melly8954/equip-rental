package com.equip.equiprental.scope.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class ManagerScopeId implements Serializable {
    private Long managerId;
    private String category;
}
