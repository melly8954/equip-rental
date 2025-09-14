package com.equip.equiprental.equipment.repository;

import com.equip.equiprental.equipment.domain.EquipmentItem;
import com.equip.equiprental.equipment.domain.EquipmentStatus;
import com.equip.equiprental.equipment.repository.dsl.EquipmentItemQRepo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface EquipmentItemRepository extends JpaRepository<EquipmentItem, Long>, EquipmentItemQRepo {
    @Query("SELECT MAX(e.sequence) FROM EquipmentItem e WHERE e.equipment.model = :model")
    Optional<Long> findMaxSequenceByModel(@Param("model") String model);

    Integer countByEquipment_EquipmentId(Long equipmentId);
    Integer countByEquipment_EquipmentIdAndStatus(Long equipmentId, EquipmentStatus status);

    @Query("SELECT e.equipmentId FROM EquipmentItem i JOIN i.equipment e WHERE i.equipmentItemId = :itemId")
    Long findEquipmentIdByItemId(@Param("itemId") Long itemId);

    @Query("""
        SELECT COUNT(ei)
        FROM EquipmentItem ei
        WHERE ei.equipment.equipmentId = :equipmentId
          AND ei.status = 'AVAILABLE'
    """)
    int countAvailableByEquipmentId(Long equipmentId);
}
