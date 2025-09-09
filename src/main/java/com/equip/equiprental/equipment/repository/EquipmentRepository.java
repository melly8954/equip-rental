package com.equip.equiprental.equipment.repository;

import com.equip.equiprental.equipment.domain.Equipment;
import com.equip.equiprental.equipment.repository.dsl.EquipmentQueryRepository;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EquipmentRepository extends JpaRepository<Equipment, Long>, EquipmentQueryRepository {
    Optional<Equipment> findByModel(String model);
    long countByModel(String modelCode);
}
