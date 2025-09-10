package com.equip.equiprental.equipment.domain;

import com.equip.equiprental.common.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name="equipment_item_tbl")
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class EquipmentItem extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="equipment_item_id")
    private Long equipmentItemId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "equipment_id")
    private Equipment equipment;

    @Column(name="serial_number")
    private String serialNumber;
    private Long sequence;

    @Enumerated(EnumType.STRING)
    private EquipmentStatus status;

    public void updateStatus(EquipmentStatus newStatus) {
        this.status = newStatus;
    }
}
