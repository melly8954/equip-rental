package com.equip.equiprental.board.repository.dsl;

import com.equip.equiprental.board.domain.BoardType;
import com.equip.equiprental.board.dto.BoardListResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface BoardQRepo {
    Page<BoardListResponse> findBoardList(Pageable pageable, BoardType type);

}
