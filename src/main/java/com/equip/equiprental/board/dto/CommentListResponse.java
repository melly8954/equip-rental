package com.equip.equiprental.board.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.querydsl.core.annotations.QueryProjection;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@AllArgsConstructor
@Builder
public class CommentListResponse {
    private Long commentId;
    private Long writerId;
    private String writerName;
    private String content;
    private Boolean isOfficial;
    private Boolean isOwner;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
    @Builder.Default
    private List<CommentListResponse> children = new ArrayList<>();

    // QueryDSL constructor projection
    @QueryProjection
    public CommentListResponse(Long commentId,
                               Long writerId,
                               String writerName,
                               String content,
                               Boolean isOfficial,
                               Boolean isOwner,
                               LocalDateTime createdAt) {
        this.commentId = commentId;
        this.writerId = writerId;
        this.writerName = writerName;
        this.content = content;
        this.isOfficial = isOfficial;
        this.isOwner = isOwner;
        this.createdAt = createdAt;
        this.children = new ArrayList<>();
    }
}
