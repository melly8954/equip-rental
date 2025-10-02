package com.equip.equiprental.equipment.repository;

import com.equip.equiprental.equipment.domain.Category;
import com.equip.equiprental.equipment.domain.Equipment;
import com.equip.equiprental.equipment.domain.EquipmentStatus;
import com.equip.equiprental.equipment.repository.dsl.EquipmentQRepo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface EquipmentRepository extends JpaRepository<Equipment, Long>, EquipmentQRepo {
    Optional<Equipment> findByEquipmentId(Long equipmentId);
    Optional<Equipment> findByModel(String model);

    @Query("SELECT e.subCategory.category FROM Equipment e WHERE e.equipmentId = :equipmentId")
    Category findCategoryByEquipmentId(@Param("equipmentId") Long equipmentId);

    @Query("SELECT COALESCE(MAX(e.modelSequence), 0) FROM Equipment e WHERE e.subCategory.subCategoryId = :subCategoryId")
    Optional<Long> findMaxModelSequence(@Param("subCategoryId") Long subCategoryId);

    @Query("""
        SELECT COUNT(e)
        FROM EquipmentItem e
        WHERE e.status IN :statuses
    """)
    int countFaultyNow(@Param("statuses") List<EquipmentStatus> statuses);

    // 긴급 관리 현황 API 조회 쿼리 메서드
    @Query(value = """
        SELECT e
        FROM Equipment e
        JOIN e.items ei
        JOIN e.subCategory sc
        JOIN sc.category c
        GROUP BY e
        HAVING SUM(CASE WHEN ei.status = 'AVAILABLE' THEN 1 ELSE 0 END) = 0
    """,
    countQuery = """
        SELECT COUNT(e)
        FROM Equipment e
        JOIN e.items ei
        GROUP BY e
        HAVING SUM(CASE WHEN ei.status = 'AVAILABLE' THEN 1 ELSE 0 END) = 0
    """)
    Page<Equipment> findZeroAvailableStock(Pageable pageable);

    @Query("""
        SELECT DISTINCT e
        FROM Equipment e
        JOIN FETCH e.subCategory sc
        JOIN FETCH sc.category c
        LEFT JOIN FETCH e.items ei
    """)
    List<Equipment> findAllWithCategorySubCategoryAndItems();
}
