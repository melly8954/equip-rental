package com.equip.equiprental.board;

import com.equip.equiprental.board.domain.Board;
import com.equip.equiprental.board.domain.Comment;
import com.equip.equiprental.board.dto.CommentCreateRequest;
import com.equip.equiprental.board.dto.CommentCreateResponse;
import com.equip.equiprental.board.dto.CommentListResponse;
import com.equip.equiprental.board.repository.BoardRepository;
import com.equip.equiprental.board.repository.CommentRepository;
import com.equip.equiprental.board.service.CommentServiceImpl;
import com.equip.equiprental.common.dto.PageResponseDto;
import com.equip.equiprental.common.dto.SearchParamDto;
import com.equip.equiprental.common.exception.CustomException;
import com.equip.equiprental.common.exception.ErrorType;
import com.equip.equiprental.member.domain.Member;
import com.equip.equiprental.member.repository.MemberRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("CommentServiceImpl 단위 테스트")
public class CommentServiceImplTest {
    @Mock private MemberRepository memberRepository;
    @Mock private BoardRepository boardRepository;
    @Mock private CommentRepository commentRepository;

    @InjectMocks
    private CommentServiceImpl commentService;

    @Nested
    @DisplayName("createComment 메서드 테스트")
    class createComment {
        @Test
        @DisplayName("성공 - 댓글 생성")
        void createComment_Success_Normal() {
            // given
            Long writerId = 1L;
            Long boardId = 10L;

            Member writer = Member.builder()
                    .memberId(writerId)
                    .name("user1")
                    .build();

            Board board = Board.builder()
                    .boardId(boardId)
                    .build();

            CommentCreateRequest dto = CommentCreateRequest.builder()
                    .boardId(boardId)
                    .content("댓글 내용")
                    .build();

            when(memberRepository.findById(writerId)).thenReturn(Optional.of(writer));
            when(boardRepository.findById(boardId)).thenReturn(Optional.of(board));

            // when
            CommentCreateResponse response = commentService.createComment(dto, writerId);

            // then
            assertThat(response).isNotNull();
            assertThat(response.getBoardId()).isEqualTo(boardId);
            assertThat(response.getWriterId()).isEqualTo(writerId);
            assertThat(response.getContent()).isEqualTo("댓글 내용");
            assertThat(response.isOfficial()).isFalse();
        }

        @Test
        @DisplayName("성공 - 대댓글 생성")
        void createComment_Reply() {
            // given
            Long writerId = 1L;
            Long boardId = 10L;
            Long parentCommentId = 100L;

            Member writer = Member.builder().memberId(writerId).build();
            Board board = Board.builder().boardId(boardId).build();
            Comment parentComment = Comment.builder().commentId(parentCommentId).build();

            CommentCreateRequest dto = CommentCreateRequest.builder()
                    .boardId(boardId)
                    .parentCommentId(parentCommentId)
                    .content("대댓글")
                    .build();

            when(memberRepository.findById(writerId)).thenReturn(Optional.of(writer));
            when(boardRepository.findById(boardId)).thenReturn(Optional.of(board));
            when(commentRepository.findById(parentCommentId)).thenReturn(Optional.of(parentComment));

            // when
            CommentCreateResponse response = commentService.createComment(dto, writerId);

            // then
            assertThat(response).isNotNull();
            assertThat(response.getBoardId()).isEqualTo(boardId);
            assertThat(response.getWriterId()).isEqualTo(writerId);
            assertThat(response.getContent()).isEqualTo("대댓글");
        }

        @Test
        @DisplayName("예외 - 작성자 없음")
        void createComment_UserNotFound() {
            // given
            Long writerId = 1L;
            CommentCreateRequest dto = CommentCreateRequest.builder().boardId(10L).content("댓글").build();

            when(memberRepository.findById(writerId)).thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> commentService.createComment(dto, writerId))
                    .isInstanceOf(CustomException.class)
                    .extracting("errorType")
                    .isEqualTo(ErrorType.USER_NOT_FOUND);
        }

        @Test
        @DisplayName("예외 - 게시글 없음")
        void createComment_BoardNotFound() {
            // given
            Long writerId = 1L;
            Long boardId = 10L;
            Member writer = Member.builder().memberId(writerId).build();

            CommentCreateRequest dto = CommentCreateRequest.builder()
                    .boardId(boardId)
                    .content("댓글")
                    .build();

            when(memberRepository.findById(writerId)).thenReturn(Optional.of(writer));
            when(boardRepository.findById(boardId)).thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> commentService.createComment(dto, writerId))
                    .isInstanceOf(CustomException.class)
                    .extracting("errorType")
                    .isEqualTo(ErrorType.BOARD_NOT_FOUND);
        }

        @Test
        @DisplayName("예외 - 부모 댓글 없음")
        void createComment_ParentNotFound() {
            // given
            Long writerId = 1L;
            Long boardId = 10L;
            Long parentCommentId = 100L;

            Member writer = Member.builder().memberId(writerId).build();
            CommentCreateRequest dto = CommentCreateRequest.builder()
                    .boardId(boardId)
                    .parentCommentId(parentCommentId)
                    .content("대댓글")
                    .build();

            when(memberRepository.findById(writerId)).thenReturn(Optional.of(writer));
            when(boardRepository.findById(boardId)).thenReturn(Optional.of(Board.builder().boardId(boardId).build()));
            when(commentRepository.findById(parentCommentId)).thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> commentService.createComment(dto, writerId))
                    .isInstanceOf(CustomException.class)
                    .extracting("errorType")
                    .isEqualTo(ErrorType.COMMENT_NOT_FOUND);
        }
    }

    @Nested
    @DisplayName("getCommentList 메서드 테스트")
    class getCommentList {
        @Test
        @DisplayName("성공 - 댓글 목록 조회")
        void getCommentList_Success_WithContent() {
            // given
            SearchParamDto paramDto = SearchParamDto.builder()
                    .boardId(10L)
                    .page(1)
                    .size(2)
                    .build();

            Long writerId = 1L;

            CommentListResponse comment1 = CommentListResponse.builder()
                    .commentId(1L)
                    .writerId(writerId)
                    .writerName("작성자1")
                    .content("댓글1")
                    .isOfficial(false)
                    .isOwner(false)
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();

            CommentListResponse comment2 = CommentListResponse.builder()
                    .commentId(2L)
                    .writerId(writerId)
                    .writerName("작성자2")
                    .content("댓글2")
                    .isOfficial(false)
                    .isOwner(false)
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();

            Pageable pageable = paramDto.getPageable();
            Page<CommentListResponse> page = new PageImpl<>(List.of(comment1, comment2), pageable, 2);

            when(commentRepository.findCommentList(pageable, paramDto.getBoardId(), writerId))
                    .thenReturn(page);

            // when
            PageResponseDto<CommentListResponse> result = commentService.getCommentList(paramDto, writerId);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(2);
            assertThat(result.getTotalElements()).isEqualTo(2);
            assertThat(result.getTotalPages()).isEqualTo(1);
            assertThat(result.isFirst()).isTrue();
            assertThat(result.isLast()).isTrue();
            assertThat(result.isEmpty()).isFalse();
        }

        @Test
        @DisplayName("성공 - 댓글 목록 빈 페이지")
        void getCommentList_Success_EmptyPage() {
            // given
            SearchParamDto paramDto = SearchParamDto.builder()
                    .boardId(10L)
                    .page(1)
                    .size(2)
                    .build();

            Long writerId = 1L;

            Pageable pageable = paramDto.getPageable();
            Page<CommentListResponse> page = new PageImpl<>(List.of(), pageable, 0);

            when(commentRepository.findCommentList(pageable, paramDto.getBoardId(), writerId))
                    .thenReturn(page);

            // when
            PageResponseDto<CommentListResponse> result = commentService.getCommentList(paramDto, writerId);

            // then
            assertThat(result.getContent()).isEmpty();
            assertThat(result.getTotalElements()).isEqualTo(0);
            assertThat(result.getTotalPages()).isEqualTo(0);
            assertThat(result.isEmpty()).isTrue();
            assertThat(result.isFirst()).isTrue();
            assertThat(result.isLast()).isTrue();
        }
    }

    @Nested
    @DisplayName("softDeleteComment 메서드 테스트")
    class softDeleteComment {
        @Test
        @DisplayName("성공 - 댓글 정상 삭제")
        void softDeleteComment_Success() {
            // given
            Long commentId = 1L;
            Comment comment = Comment.builder()
                    .commentId(commentId)
                    .isDeleted(false)
                    .build();

            when(commentRepository.findById(commentId)).thenReturn(Optional.of(comment));

            // when
            commentService.softDeleteComment(commentId);

            // then
            assertThat(comment.getIsDeleted()).isTrue(); // softDelete() 적용 여부 확인
        }

        @Test
        @DisplayName("예외 - 댓글 없음")
        void softDeleteComment_NotFound() {
            // given
            Long commentId = 1L;
            when(commentRepository.findById(commentId)).thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> commentService.softDeleteComment(commentId))
                    .isInstanceOf(CustomException.class)
                    .extracting("errorType")
                    .isEqualTo(ErrorType.COMMENT_NOT_FOUND);
        }

        @Test
        @DisplayName("예외 - 이미 삭제된 댓글")
        void softDeleteComment_AlreadyDeleted() {
            // given
            Long commentId = 1L;
            Comment comment = Comment.builder()
                    .commentId(commentId)
                    .isDeleted(true)
                    .build();

            when(commentRepository.findById(commentId)).thenReturn(Optional.of(comment));

            // when & then
            assertThatThrownBy(() -> commentService.softDeleteComment(commentId))
                    .isInstanceOf(CustomException.class)
                    .extracting("errorType")
                    .isEqualTo(ErrorType.ALREADY_DELETED);
        }
    }
}
