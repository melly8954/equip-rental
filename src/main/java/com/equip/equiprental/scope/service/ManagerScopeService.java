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

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ManagerScopeService {
    private final EquipmentRepository equipmentRepository;
    private final EquipmentItemRepository equipmentItemRepository;
    private final MemberRepository memberRepository;
    private final CategoryRepository categoryRepository;
    private final ManagerScopeRepository managerScopeRepository;

    @Transactional
    public void setScope(Long managerId, List<Long> categoryIds) {
        Member manager = memberRepository.findById(managerId)
                .orElseThrow(() -> new CustomException(ErrorType.USER_NOT_FOUND));

        List<ManagerScope> existingScopes = managerScopeRepository.findAllByManager(manager);
        Set<Long> existingCategoryIds = existingScopes.stream()
                .map(scope -> scope.getCategory().getCategoryId())
                .collect(Collectors.toSet());

        Set<Long> newCategoryIds = categoryIds == null ? Collections.emptySet() : new HashSet<>(categoryIds);

        // 삭제할 것: 기존에 있고, 새 선택에는 없는 것
        existingScopes.stream()
                .filter(scope -> !newCategoryIds.contains(scope.getCategory().getCategoryId()))
                .forEach(managerScopeRepository::delete);

        // 추가할 것: 새 선택에 있고, 기존에는 없는 것
        newCategoryIds.stream()
                .filter(catId -> !existingCategoryIds.contains(catId))
                .forEach(catId -> {
                    Category category = categoryRepository.findById(catId)
                            .orElseThrow(() -> new CustomException(ErrorType.INVALID_CATEGORY_REQUEST));
                    ManagerScope scope = ManagerScope.builder()
                            .manager(manager)
                            .category(category)
                            .build();
                    managerScopeRepository.save(scope);
                });
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
