package com.equip.equiprental.board.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class CommentCreateResponse {
    private Long commentId;
    private Long boardId;
    private Long writerId;
    private boolean isOfficial;
    private String content;
}
