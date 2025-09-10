package com.equip.equiprental.equipment.repository;

import com.equip.equiprental.equipment.domain.EquipmentItem;
import com.equip.equiprental.equipment.domain.EquipmentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EquipmentItemRepository extends JpaRepository<EquipmentItem, Long> {
    Page<EquipmentItem> findByEquipment_EquipmentId(Long equipmentId, Pageable pageable);
    Page<EquipmentItem> findByEquipment_EquipmentIdAndStatus(Long equipmentId, EquipmentStatus status, Pageable pageable);
}
