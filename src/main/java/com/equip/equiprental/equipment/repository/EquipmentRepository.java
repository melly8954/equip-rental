package com.equip.equiprental.equipment.repository;

import com.equip.equiprental.equipment.domain.Equipment;
import com.equip.equiprental.equipment.domain.EquipmentCategory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface EquipmentRepository extends JpaRepository<Equipment, Long> {
    Optional<Equipment> findByModel(String model);
    long countByModel(String modelCode);

    @Query("SELECT e FROM Equipment e " +
            "WHERE (:category IS NULL OR :category = '' OR e.category = :category) " +
            "AND (:subCategory IS NULL OR :subCategory = '' OR e.subCategory = :subCategory) " +
            "AND (:model IS NULL OR :model = '' OR e.model LIKE CONCAT('%', :model, '%'))")
    Page<Equipment> findByFilters(@Param("category") EquipmentCategory category,
                                  @Param("subCategory") String subCategory,
                                  @Param("model") String model,
                                  Pageable pageable);
}
