package com.equip.equiprental.filestorage.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class StoredFile {
    private final String uuid;          // DB에 저장할 고유 식별자
    private final String localFileName; // 실제 로컬 파일명
    private final String originalName;  // 업로드 원본 파일명
}
