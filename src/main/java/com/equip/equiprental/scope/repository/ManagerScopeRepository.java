package com.equip.equiprental.scope.repository;

import com.equip.equiprental.scope.domain.ManagerScope;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ManagerScopeRepository extends JpaRepository<ManagerScope, Integer> {
    boolean existsByManagerIdAndCategory(Long managerId, String category);
}
