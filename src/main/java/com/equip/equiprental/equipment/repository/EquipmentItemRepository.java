package com.equip.equiprental.equipment.repository;

import com.equip.equiprental.equipment.domain.EquipmentItem;
import com.equip.equiprental.equipment.domain.EquipmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface EquipmentItemRepository extends JpaRepository<EquipmentItem, Long> {
    @Query("SELECT COUNT(i) FROM EquipmentItem i " +
            "WHERE i.equipment.equipmentId = :equipmentId " +
            "AND i.status = :status")
    int countByStatus(@Param("equipmentId") Long equipmentId,
                       @Param("status") EquipmentStatus status);
}
