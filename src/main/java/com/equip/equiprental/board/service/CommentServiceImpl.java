package com.equip.equiprental.board.service;

import com.equip.equiprental.board.domain.Board;
import com.equip.equiprental.board.domain.Comment;
import com.equip.equiprental.board.dto.CommentCreateRequest;
import com.equip.equiprental.board.dto.CommentCreateResponse;
import com.equip.equiprental.board.dto.CommentListResponse;
import com.equip.equiprental.board.repository.BoardRepository;
import com.equip.equiprental.board.repository.CommentRepository;
import com.equip.equiprental.common.dto.PageResponseDto;
import com.equip.equiprental.common.dto.SearchParamDto;
import com.equip.equiprental.common.exception.CustomException;
import com.equip.equiprental.common.exception.ErrorType;
import com.equip.equiprental.member.domain.Member;
import com.equip.equiprental.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService{
    private final MemberRepository memberRepository;
    private final BoardRepository boardRepository;
    private final CommentRepository commentRepository;

    @Override
    @Transactional
    public CommentCreateResponse createComment(CommentCreateRequest dto, Long writerId) {
        Member writer = memberRepository.findById(writerId)
                .orElseThrow(() -> new CustomException(ErrorType.USER_NOT_FOUND));

        Board board = boardRepository.findById(dto.getBoardId())
                .orElseThrow(() -> new CustomException(ErrorType.BOARD_NOT_FOUND));

        // parentCommentId가 있으면 찾아오기
        Comment parent = null;
        if (dto.getParentCommentId() != null) {
            parent = commentRepository.findById(dto.getParentCommentId())
                    .orElseThrow(() -> new CustomException(ErrorType.COMMENT_NOT_FOUND));
        }

        boolean isOfficial = writer.isAdminOrManager();

        Comment comment = Comment.builder()
                .board(board)
                .writer(writer)
                .parent(parent)
                .isOfficial(isOfficial)
                .content(dto.getContent())
                .isDeleted(false)
                .build();
        commentRepository.save(comment);

        return CommentCreateResponse.builder()
                .commentId(comment.getCommentId())
                .boardId(comment.getBoard().getBoardId())
                .writerId(comment.getWriter().getMemberId())
                .isOfficial(comment.getIsOfficial())
                .content(comment.getContent())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponseDto<CommentListResponse> getCommentList(SearchParamDto paramDto, Long writerId) {
        Pageable pageable = paramDto.getPageable();

        Page<CommentListResponse> dtosPage = commentRepository.findCommentList(pageable, paramDto.getBoardId(), writerId);

        return PageResponseDto.<CommentListResponse>builder()
                .content(dtosPage.getContent())
                .page(dtosPage.getNumber() + 1)
                .size(dtosPage.getSize())
                .totalElements(dtosPage.getTotalElements())
                .totalPages(dtosPage.getTotalPages())
                .numberOfElements(dtosPage.getNumberOfElements())
                .first(dtosPage.isFirst())
                .last(dtosPage.isLast())
                .empty(dtosPage.isEmpty())
                .build();
    }

    @Override
    @Transactional
    public void softDeleteComment(Long commentId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new CustomException(ErrorType.COMMENT_NOT_FOUND));

        if (comment.getIsDeleted()) {
            throw new CustomException(ErrorType.ALREADY_DELETED);
        }

        comment.softDelete();
    }
}
