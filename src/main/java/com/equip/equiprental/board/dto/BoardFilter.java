package com.equip.equiprental.board.dto;

import com.equip.equiprental.board.domain.BoardType;
import com.equip.equiprental.common.dto.SearchParamDto;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class BoardFilter extends SearchParamDto {
    private BoardType boardType;
    private String searchType;
    private String keyword;
}
