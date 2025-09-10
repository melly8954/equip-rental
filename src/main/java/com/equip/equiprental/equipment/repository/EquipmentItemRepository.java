package com.equip.equiprental.equipment.repository;

import com.equip.equiprental.equipment.domain.EquipmentItem;
import com.equip.equiprental.equipment.repository.dsl.EquipmentItemQRepo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface EquipmentItemRepository extends JpaRepository<EquipmentItem, Long>, EquipmentItemQRepo {
    @Query("SELECT i FROM EquipmentItem i JOIN FETCH i.equipment WHERE i.equipmentItemId = :id")
    Optional<EquipmentItem> findByIdWithEquipment(@Param("id") Long id);

    @Query("SELECT MAX(e.sequence) FROM EquipmentItem e WHERE e.equipment.model = :model")
    Optional<Long> findMaxSequenceByModel(@Param("model") String model);
}
