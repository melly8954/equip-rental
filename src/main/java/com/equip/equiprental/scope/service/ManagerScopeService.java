package com.equip.equiprental.scope.service;

import com.equip.equiprental.equipment.domain.Category;
import com.equip.equiprental.equipment.repository.EquipmentItemRepository;
import com.equip.equiprental.equipment.repository.EquipmentRepository;
import com.equip.equiprental.scope.repository.ManagerScopeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ManagerScopeService {
    private final EquipmentRepository equipmentRepository;
    private final EquipmentItemRepository equipmentItemRepository;
    private final ManagerScopeRepository managerScopeRepository;

    /**
     * Equipment 단위 권한 체크
     */
    public boolean canAccessEquipment(Long equipmentId, Long managerId) {
        Category category = equipmentRepository.findCategoryByEquipmentId(equipmentId);
        if (category == null) return false;

        // enum -> String
        String label = category.getLabel();
        return managerScopeRepository.existsByManagerIdAndCategory(managerId, label);
    }

    /**
     * EquipmentItem 단위 권한 체크
     */
    public boolean canAccessEquipmentByItem(Long equipmentItemId, Long managerId) {
        // item → equipmentId 조회
        Long equipmentId = equipmentItemRepository.findEquipmentIdByItemId(equipmentItemId);
        if (equipmentId == null) return false;

        // 기존 equipment 단위 체크 재사용
        return canAccessEquipment(equipmentId, managerId);
    }
}
