package com.equip.equiprental.equipment.repository;

import com.equip.equiprental.equipment.domain.EquipmentItem;
import com.equip.equiprental.equipment.domain.EquipmentStatus;
import com.equip.equiprental.equipment.repository.dsl.EquipmentItemQRepo;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
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

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT i FROM EquipmentItem i WHERE i.equipment.equipmentId = :equipmentId AND i.status = 'AVAILABLE' ORDER BY i.createdAt ASC")
    List<EquipmentItem> findAvailableItemsForUpdate(@Param("equipmentId") Long equipmentId, Pageable limit);

    @Modifying
    @Query("UPDATE EquipmentItem i SET i.status = 'RENTED' WHERE i.equipmentItemId IN :itemIds")
    int approveRental(@Param("itemIds") List<Long> itemIds);
}
