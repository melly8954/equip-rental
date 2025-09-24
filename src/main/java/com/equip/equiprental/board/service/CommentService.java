package com.equip.equiprental.board.service;

import com.equip.equiprental.board.dto.CommentCreateRequest;
import com.equip.equiprental.board.dto.CommentCreateResponse;

public interface CommentService {
    CommentCreateResponse createComment(CommentCreateRequest dto, Long writerId);
}
