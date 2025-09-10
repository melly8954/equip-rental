package com.equip.equiprental.scope.service;

import com.equip.equiprental.equipment.domain.EquipmentCategory;
import com.equip.equiprental.equipment.repository.EquipmentRepository;
import com.equip.equiprental.scope.repository.ManagerScopeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ManagerScopeService {
    private final EquipmentRepository equipmentRepository;
    private final ManagerScopeRepository managerScopeRepository;

    public boolean canAccessEquipment(Long equipmentId, Long managerId) {
        EquipmentCategory categoryEnum = equipmentRepository.findCategoryByEquipmentId(equipmentId);
        if (categoryEnum == null) return false;

        // enum -> String
        String category = categoryEnum.name();
        return managerScopeRepository.existsByManagerIdAndCategory(managerId, category);
    }
}
