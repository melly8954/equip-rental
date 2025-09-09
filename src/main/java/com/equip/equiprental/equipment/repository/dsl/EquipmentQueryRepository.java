package com.equip.equiprental.equipment.repository.dsl;

import com.equip.equiprental.common.dto.SearchParamDto;
import com.equip.equiprental.equipment.dto.EquipmentDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface EquipmentQueryRepository {
    Page<EquipmentDto> findByFilters(SearchParamDto paramDto, Pageable pageable);
}
