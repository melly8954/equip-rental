package com.equip.equiprental.member.domain;

import com.equip.equiprental.common.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name="department_tbl")
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Department extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="department_id")
    private Long departmentId;

    @Column(name="department_name")
    private String departmentName;
}
