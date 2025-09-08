package com.equip.equiprental.equipment.domain;

import com.equip.equiprental.common.domain.BaseEntity;
import com.equip.equiprental.member.domain.Member;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name="equipment_item_history_tbl")
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class EquipmentItemHistory extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="history_id")
    private Long historyId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "equipment_item_id")
    private EquipmentItem item;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "changed_by")
    private Member changedBy;

    @Column(name="old_status")
    private EquipmentStatus oldStatus;

    @Column(name="new_status")
    private EquipmentStatus newStatus;
}
