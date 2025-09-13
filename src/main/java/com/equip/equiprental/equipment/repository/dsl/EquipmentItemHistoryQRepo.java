package com.equip.equiprental.equipment.repository.dsl;

import com.equip.equiprental.equipment.dto.EquipmentItemHistoryDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface EquipmentItemHistoryQRepo {
    Page<EquipmentItemHistoryDto> findHistoriesByEquipmentItemId(Long equipmentItemId, Pageable pageable);
}
