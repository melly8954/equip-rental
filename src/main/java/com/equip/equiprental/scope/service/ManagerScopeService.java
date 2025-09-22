package com.equip.equiprental.scope.service;

import com.equip.equiprental.common.exception.CustomException;
import com.equip.equiprental.common.exception.ErrorType;
import com.equip.equiprental.equipment.domain.Category;
import com.equip.equiprental.equipment.repository.CategoryRepository;
import com.equip.equiprental.equipment.repository.EquipmentItemRepository;
import com.equip.equiprental.equipment.repository.EquipmentRepository;
import com.equip.equiprental.member.domain.Member;
import com.equip.equiprental.member.repository.MemberRepository;
import com.equip.equiprental.scope.domain.ManagerScope;
import com.equip.equiprental.scope.repository.ManagerScopeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ManagerScopeService {
    private final EquipmentRepository equipmentRepository;
    private final EquipmentItemRepository equipmentItemRepository;
    private final MemberRepository memberRepository;
    private final CategoryRepository categoryRepository;
    private final ManagerScopeRepository managerScopeRepository;

    @Transactional
    public void setScope(Long managerId, Long categoryId) {
        Member manager = memberRepository.findById(managerId)
                .orElseThrow(() -> new CustomException(ErrorType.USER_NOT_FOUND));

        Optional<ManagerScope> existingScope = managerScopeRepository.findByManager(manager);

        if (categoryId == null) {
            // 미지정 선택 → 기존 scope 삭제
            existingScope.ifPresent(managerScopeRepository::delete);
            return;
        }

        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new CustomException(ErrorType.INVALID_CATEGORY_REQUEST));

        if (existingScope.isPresent()) {
            // 기존 레코드 업데이트
            ManagerScope scope = existingScope.get();
            scope = ManagerScope.builder()
                    .scopeId(scope.getScopeId())
                    .manager(manager)
                    .category(category)
                    .build();
            managerScopeRepository.save(scope);
        } else {
            // 새로 생성
            ManagerScope scope = ManagerScope.builder()
                    .manager(manager)
                    .category(category)
                    .build();
            managerScopeRepository.save(scope);
        }
    }

    /**
     * Equipment 단위 권한 체크
     */
    public boolean canAccessEquipment(Long equipmentId, Long managerId) {
        Category category = equipmentRepository.findCategoryByEquipmentId(equipmentId);
        if (category == null) return false;

        Long categoryId = category.getCategoryId();
        return managerScopeRepository.existsByManager_MemberIdAndCategory_CategoryId(managerId, categoryId);
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
