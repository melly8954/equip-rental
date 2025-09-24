package com.equip.equiprental.board.dto;

import com.equip.equiprental.board.domain.BoardType;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@AllArgsConstructor
@Builder
public class BoardDetailDto {
    private Long boardId;
    private BoardType boardType;
    private String title;
    private String content;
    private LocalDateTime createdAt;
    private List<String> filePath;
    @JsonProperty("isOwner")
    private boolean owner;
}
