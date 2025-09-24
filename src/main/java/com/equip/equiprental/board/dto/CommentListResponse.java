package com.equip.equiprental.board.dto;

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
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    @Builder.Default
    private List<CommentListResponse> children = new ArrayList<>();

    // QueryDSL constructor projection
    @QueryProjection
    public CommentListResponse(Long commentId,
                               Long writerId,
                               String writerName,
                               String content,
                               Boolean isOfficial,
                               LocalDateTime createdAt,
                               LocalDateTime updatedAt) {
        this.commentId = commentId;
        this.writerId = writerId;
        this.writerName = writerName;
        this.content = content;
        this.isOfficial = isOfficial;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.children = new ArrayList<>();
    }
}
