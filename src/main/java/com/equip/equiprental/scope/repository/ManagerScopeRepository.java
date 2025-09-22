package com.equip.equiprental.scope.repository;

import com.equip.equiprental.equipment.domain.Category;
import com.equip.equiprental.member.domain.Member;
import com.equip.equiprental.scope.domain.ManagerScope;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ManagerScopeRepository extends JpaRepository<ManagerScope, Integer> {
    boolean existsByManager_MemberIdAndCategory_CategoryId(Long managerId, Long categoryId);

    @Query("SELECT ms.category FROM ManagerScope ms WHERE ms.manager.memberId = :managerId")
    List<Category> findCategoriesByManager(@Param("managerId") Long managerId);

    List<ManagerScope> findAllByManager(Member manager);
}
