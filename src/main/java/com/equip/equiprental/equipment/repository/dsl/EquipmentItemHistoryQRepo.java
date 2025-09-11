package com.equip.equiprental.equipment.repository.dsl;

import com.equip.equiprental.equipment.domain.EquipmentItemHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface EquipmentItemHistoryQRepo {
    Page<EquipmentItemHistory> findByEquipmentItemIdWithMember(Long equipmentItemId, Pageable pageable);
}
