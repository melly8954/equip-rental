package com.equip.equiprental.board.service.iface;

import com.equip.equiprental.board.dto.*;
import com.equip.equiprental.common.dto.PageResponseDto;
import com.equip.equiprental.common.dto.SearchParamDto;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface BoardService {
    BoardCreateResponse createBoard(BoardCreateRequest dto, List<MultipartFile> files, Long writerId);

    PageResponseDto<BoardListResponse> getBoardList(SearchParamDto paramDto);
    List<BoardListResponse> getLatestNotices(int i);

    BoardDetailDto getBoardDetail(Long boardId, Long currentUserId);

    void softDeleteBoard(Long boardId);

    BoardUpdateResponse updateBoard(Long boardId, BoardUpdateRequest boardCreateRequest, List<MultipartFile> files);
}
