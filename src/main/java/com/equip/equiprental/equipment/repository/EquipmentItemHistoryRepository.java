package com.equip.equiprental.equipment.repository;

import com.equip.equiprental.equipment.domain.EquipmentItemHistory;
import com.equip.equiprental.equipment.repository.dsl.EquipmentItemHistoryQRepo;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EquipmentItemHistoryRepository extends JpaRepository<EquipmentItemHistory, Long>, EquipmentItemHistoryQRepo {
}
