package com.equip.equiprental.board.service;

import com.equip.equiprental.board.dto.BoardCreateRequest;
import com.equip.equiprental.board.dto.BoardCreateResponse;
import com.equip.equiprental.board.dto.BoardDetailDto;
import com.equip.equiprental.board.dto.BoardListResponse;
import com.equip.equiprental.common.dto.PageResponseDto;
import com.equip.equiprental.common.dto.SearchParamDto;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface BoardService {
    BoardCreateResponse createBoard(BoardCreateRequest dto, List<MultipartFile> files, Long writerId);

    PageResponseDto<BoardListResponse> getBoardList(SearchParamDto paramDto);
    List<BoardListResponse> getLatestNotices(int i);

    BoardDetailDto getBoardDetail(Long boardId);
}
