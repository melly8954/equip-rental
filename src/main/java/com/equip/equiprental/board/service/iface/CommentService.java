package com.equip.equiprental.board.service.iface;

import com.equip.equiprental.board.dto.CommentCreateRequest;
import com.equip.equiprental.board.dto.CommentCreateResponse;
import com.equip.equiprental.board.dto.CommentListResponse;
import com.equip.equiprental.common.dto.PageResponseDto;
import com.equip.equiprental.common.dto.SearchParamDto;

public interface CommentService {
    CommentCreateResponse createComment(CommentCreateRequest dto, Long writerId);

    PageResponseDto<CommentListResponse> getCommentList(SearchParamDto paramDto, Long writerId);

    void softDeleteComment(Long commentId);
}
