package com.equip.equiprental.equipment.repository.dsl;

import com.equip.equiprental.equipment.domain.EquipmentStatus;
import com.equip.equiprental.equipment.dto.EquipmentItemListDto;
import org.springframework.data.domain.Pageable;

public interface EquipmentItemQRepo {
    EquipmentItemListDto findEquipmentItemByFilter(Long equipmentId, EquipmentStatus status, Pageable pageable);
}
