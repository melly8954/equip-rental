package com.equip.equiprental.equipment.repository;

import com.equip.equiprental.equipment.domain.SubCategory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SubCategoryRepository extends JpaRepository<SubCategory, Long> {
    List<SubCategory> findByCategoryCategoryId(Long categoryId);
}
