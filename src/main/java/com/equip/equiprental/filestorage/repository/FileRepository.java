package com.equip.equiprental.filestorage.repository;

import com.equip.equiprental.filestorage.domain.FileMeta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface FileRepository extends JpaRepository<FileMeta, Long> {
    @Query("SELECT f.filePath FROM FileMeta f " +
            "WHERE f.relatedId = :equipmentId " +
            "ORDER BY f.createdAt ASC")
    List<String> findUrlsByEquipmentId(@Param("equipmentId") Long equipmentId);
}
