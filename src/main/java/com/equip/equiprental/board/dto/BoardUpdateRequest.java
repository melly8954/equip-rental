package com.equip.equiprental.board.dto;

import com.equip.equiprental.board.domain.BoardType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class BoardUpdateRequest {
    private BoardType boardType;
    private String title;
    private String content;
}
