package com.equip.equiprental.scope.repository;

import com.equip.equiprental.equipment.domain.Category;
import com.equip.equiprental.member.domain.Member;
import com.equip.equiprental.scope.domain.ManagerScope;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ManagerScopeRepository extends JpaRepository<ManagerScope, Integer> {
    boolean existsByManager_MemberIdAndCategory_CategoryId(Long managerId, Long categoryId);

    @Query("SELECT m.category FROM ManagerScope m WHERE m.manager.memberId = :memberId")
    Category findCategoryByManager(@Param("memberId") Long memberId);

    Optional<ManagerScope> findByManager(Member manager);
}
