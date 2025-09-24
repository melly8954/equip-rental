package com.equip.equiprental.board.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class BoardFileDto {
    private Long fileId;             // 파일 식별자
    private String originalName; // 업로드 당시 이름
    private String filePath;     // 저장된 경로
}