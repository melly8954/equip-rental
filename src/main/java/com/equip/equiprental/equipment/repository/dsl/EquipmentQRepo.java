package com.equip.equiprental.equipment.repository.dsl;

import com.equip.equiprental.equipment.dto.EquipmentDto;
import com.equip.equiprental.equipment.dto.EquipmentFilter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface EquipmentQRepo {
    Page<EquipmentDto> findByFilters(EquipmentFilter paramDto, Pageable pageable);
}
