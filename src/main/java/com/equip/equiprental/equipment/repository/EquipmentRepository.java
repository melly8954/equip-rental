package com.equip.equiprental.equipment.repository;

import com.equip.equiprental.equipment.domain.Equipment;
import com.equip.equiprental.equipment.domain.EquipmentCategory;
import com.equip.equiprental.equipment.repository.dsl.EquipmentQRepo;
import com.equip.equiprental.rental.repository.dsl.RentalQRepo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface EquipmentRepository extends JpaRepository<Equipment, Long>, EquipmentQRepo, RentalQRepo {
    Optional<Equipment> findByEquipmentId(Long equipmentId);
    Optional<Equipment> findByModel(String model);

    @Query("SELECT e.category FROM Equipment e WHERE e.equipmentId = :equipmentId")
    EquipmentCategory findCategoryByEquipmentId(@Param("equipmentId") Long equipmentId);
}
