package com.equip.equiprental.equipment.repository;

import com.equip.equiprental.equipment.domain.EquipmentItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EquipmentItemRepository extends JpaRepository<EquipmentItem, Long> {
    boolean existsBySerialNumber(String serialNumber); // 랜덤 중복 체크용
}
