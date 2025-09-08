package com.equip.equiprental.filestorage.repository;

import com.equip.equiprental.filestorage.domain.FileMeta;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FileRepository extends JpaRepository<FileMeta, Long> {
}
