package com.equip.equiprental.equipment.domain;

import com.equip.equiprental.common.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name="equipment_tbl")
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Equipment extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="equipment_id")
    private Long equipmentId;

    @Enumerated(EnumType.STRING)
    private EquipmentCategory category;

    @Column(name="sub_category")
    private String subCategory;

    private String model;
    private Integer stock;
}
