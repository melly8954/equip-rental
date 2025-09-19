package com.equip.equiprental.scope.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name="manager_scope_tbl")
@IdClass(ManagerScopeId.class)
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ManagerScope {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="manager_id")
    private Long managerId;

    @Id
    private String  category;
}
