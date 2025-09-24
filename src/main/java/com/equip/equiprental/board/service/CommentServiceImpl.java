package com.equip.equiprental.board.service;

import com.equip.equiprental.board.domain.Board;
import com.equip.equiprental.board.domain.Comment;
import com.equip.equiprental.board.dto.CommentCreateRequest;
import com.equip.equiprental.board.dto.CommentCreateResponse;
import com.equip.equiprental.board.repository.BoardRepository;
import com.equip.equiprental.board.repository.CommentRepository;
import com.equip.equiprental.common.exception.CustomException;
import com.equip.equiprental.common.exception.ErrorType;
import com.equip.equiprental.member.domain.Member;
import com.equip.equiprental.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
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

        boolean isOfficial = writer.isAdminOrManager();

        Comment comment = Comment.builder()
                .board(board)
                .writer(writer)
                .isOfficial(isOfficial)
                .content(dto.getContent())
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
}
