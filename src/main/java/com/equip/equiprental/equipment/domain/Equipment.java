package com.equip.equiprental.equipment.domain;

import com.equip.equiprental.common.domain.BaseEntity;
import com.equip.equiprental.common.exception.CustomException;
import com.equip.equiprental.common.exception.ErrorType;
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

    @Column(name="model_code")
    private String modelCode;

    private Integer stock;

    public void increaseStock(int amount) {
        if (amount < 0) {
            throw new CustomException(ErrorType.AMOUNT_MUST_BE_POSITIVE);
        }
        this.stock += amount; // 기존 객체 직접 변경
    }
}
