package com.equip.equiprental.board.repository.dsl;

import com.equip.equiprental.board.dto.CommentListResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CommentQRepo {
    Page<CommentListResponse> findCommentList(Pageable pageable, Long boardId);
}
