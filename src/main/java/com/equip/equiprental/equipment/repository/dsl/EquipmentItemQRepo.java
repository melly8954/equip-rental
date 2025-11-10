package com.equip.equiprental.equipment.repository.dsl;

import com.equip.equiprental.equipment.domain.EquipmentStatus;
import com.equip.equiprental.equipment.dto.EquipmentItemDto;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface EquipmentItemQRepo {
    Page<EquipmentItemDto> findByStatus(Long equipmentId, EquipmentStatus status, Pageable pageable);
}
