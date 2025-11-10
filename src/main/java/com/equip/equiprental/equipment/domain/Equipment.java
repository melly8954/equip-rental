package com.equip.equiprental.equipment.domain;

import com.equip.equiprental.common.domain.BaseEntity;
import com.equip.equiprental.common.exception.CustomException;
import com.equip.equiprental.common.exception.ErrorType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="sub_category_id")
    private SubCategory subCategory;

    private String model;

    @Column(name="model_code")
    private String modelCode;

    @Column(name="model_sequence")
    private Long modelSequence;

    private Integer stock;

    @Column(name="is_deleted")
    private boolean deleted;

    @Column(name="deleted_at")
    private LocalDateTime deletedAt;

    @OneToMany(mappedBy = "equipment")
    private List<EquipmentItem> items;

    public void increaseStock(int amount) {
        if (amount < 0) {
            throw new CustomException(ErrorType.BAD_REQUEST, "추가할 재고 수량은 반드시 양수여야합니다.");
        }
        this.stock += amount; // 기존 객체 직접 변경
    }

    public void softDelete() {
        this.deleted = true;
        this.deletedAt = LocalDateTime.now();
    }
}
