package com.equip.equiprental.board.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@AllArgsConstructor
@Builder
public class BoardCreateResponse {
    private Long boardId;
    private String boardType;
    private String title;
    private String content;
    private String status;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
    private List<BoardFile> files;

    @Getter
    @AllArgsConstructor
    @Builder
    public static class BoardFile {
        private Long fileId;
        private String originalName;
        private String url;
        private Integer fileOrder;
        private String fileType;
        private Long fileSize;
    }
}
