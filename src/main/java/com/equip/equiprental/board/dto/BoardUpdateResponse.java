package com.equip.equiprental.board.dto;

import com.equip.equiprental.board.domain.BoardType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
@Builder
public class BoardUpdateResponse {
    private Long boardId;
    private BoardType boardType;
    private String title;
    private String content;
    private List<BoardFileDto> files;
}
