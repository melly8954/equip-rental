package com.equip.equiprental.equipment.repository;

import com.equip.equiprental.equipment.domain.Category;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<Category, Long> {
}
