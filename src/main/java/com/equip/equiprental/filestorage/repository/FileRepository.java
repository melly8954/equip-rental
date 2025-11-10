package com.equip.equiprental.filestorage.repository;

import com.equip.equiprental.filestorage.domain.FileMeta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

public interface FileRepository extends JpaRepository<FileMeta, Long> {
    @Query("SELECT f.filePath FROM FileMeta f " +
            "WHERE f.relatedId = :equipmentId " +
            "ORDER BY f.createdAt ASC")
    // 파일 url 리스트 (ASC 조회로 가장 앞 대표 이미지 url 얻을 수 있음)
    List<String> findUrlsByEquipmentId(@Param("equipmentId") Long equipmentId);

    List<FileMeta> findByRelatedTypeAndRelatedId(String equipment, Long equipmentId);

    List<FileMeta> findAllByRelatedTypeAndRelatedId(String relatedType, Long boardId);
}
