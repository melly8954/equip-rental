package com.equip.equiprental.equipment.repository;

import com.equip.equiprental.equipment.domain.EquipmentItem;
import com.equip.equiprental.equipment.repository.dsl.EquipmentItemQRepo;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EquipmentItemRepository extends JpaRepository<EquipmentItem, Long>, EquipmentItemQRepo {
}
