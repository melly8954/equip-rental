package com.equip.equiprental.board.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class CommentCreateRequest {
    private Long boardId;
    private Long parentCommentId;
    private String content;
}
