package com.equip.equiprental.board;


import com.equip.equiprental.board.domain.Board;
import com.equip.equiprental.board.domain.BoardStatus;
import com.equip.equiprental.board.domain.BoardType;
import com.equip.equiprental.board.dto.*;
import com.equip.equiprental.board.repository.BoardRepository;
import com.equip.equiprental.board.service.BoardServiceImpl;
import com.equip.equiprental.common.dto.PageResponseDto;
import com.equip.equiprental.common.dto.SearchParamDto;
import com.equip.equiprental.common.exception.CustomException;
import com.equip.equiprental.common.exception.ErrorType;
import com.equip.equiprental.filestorage.domain.FileMeta;
import com.equip.equiprental.filestorage.repository.FileRepository;
import com.equip.equiprental.filestorage.service.iface.FileService;
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
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("BoardServiceImpl 단위 테스트")
public class BoardServiceImplTest {
    @Mock private BoardRepository boardRepository;
    @Mock private MemberRepository memberRepository;
    @Mock private FileRepository fileRepository;
    @Mock private FileService fileService;

    @InjectMocks
    private BoardServiceImpl boardService;

    @Nested
    @DisplayName("createBoard 메서드 테스트")
    class createBoard {
        @Test
        @DisplayName("성공 - 게시글 생성 (파일 포함)")
        void createBoard_Success_WithFiles() throws IOException {
            Member member = mock(Member.class);
            when(member.isAdminOrManager()).thenReturn(true);
            when(memberRepository.findById(1L)).thenReturn(Optional.of(member));

            BoardCreateRequest dto = BoardCreateRequest.builder()
                    .boardType(BoardType.NOTICE)
                    .title("제목")
                    .content("내용")
                    .build();

            Board savedBoard = Board.builder()
                    .boardId(100L)
                    .writer(member)
                    .boardType(dto.getBoardType())
                    .title(dto.getTitle())
                    .content(dto.getContent())
                    .status(BoardStatus.PENDING)
                    .isDeleted(false)
                    .build();

            when(boardRepository.save(any(Board.class))).thenReturn(savedBoard);

            MultipartFile file = mock(MultipartFile.class);
            when(file.getOriginalFilename()).thenReturn("file.txt");
            when(file.getContentType()).thenReturn("text/plain");
            when(file.getSize()).thenReturn(123L);

            List<MultipartFile> files = List.of(file);
            List<String> fileUrls = List.of("http://test.com/files/file.txt");
            when(fileService.saveFiles(files, "board_notice")).thenReturn(fileUrls);

            BoardCreateResponse response = boardService.createBoard(dto, files, 1L);

            assertThat(response.getFiles()).hasSize(1);
            assertThat(response.getFiles().get(0).getOriginalName()).isEqualTo("file.txt");

            verify(boardRepository).save(any(Board.class));
            verify(fileService).saveFiles(files, "board_notice");
            verify(fileRepository).saveAll(anyList());
        }

        @Test
        @DisplayName("성공 - 게시글 생성 (파일 미포함)")
        void createBoard_Success_NoFiles() {
            Member member = mock(Member.class);
            when(member.isAdminOrManager()).thenReturn(true);
            when(memberRepository.findById(1L)).thenReturn(Optional.of(member));

            BoardCreateRequest dto = BoardCreateRequest.builder()
                    .boardType(BoardType.NOTICE)
                    .title("제목")
                    .content("내용")
                    .build();

            Board savedBoard = Board.builder()
                    .boardId(100L)
                    .writer(member)
                    .boardType(dto.getBoardType())
                    .title(dto.getTitle())
                    .content(dto.getContent())
                    .status(BoardStatus.PENDING)
                    .isDeleted(false)
                    .build();

            when(boardRepository.save(any(Board.class))).thenAnswer(invocation -> {
                Board board = invocation.getArgument(0);

                // boardId reflection
                java.lang.reflect.Field boardIdField = Board.class.getDeclaredField("boardId");
                boardIdField.setAccessible(true);
                boardIdField.set(board, 100L);

                // createdAt reflection (BaseEntity)
                java.lang.reflect.Field createdAtField = Board.class.getSuperclass().getDeclaredField("createdAt");
                createdAtField.setAccessible(true);
                createdAtField.set(board, java.time.LocalDateTime.now());

                return board;
            });

            BoardCreateResponse response = boardService.createBoard(dto, null, 1L);

            assertThat(response).isNotNull();
            assertThat(response.getBoardId()).isEqualTo(100L);
            assertThat(response.getFiles()).isEmpty();
            verify(boardRepository).save(any(Board.class));
            verifyNoInteractions(fileService, fileRepository);
        }

        @Test
        @DisplayName("예외 - 존재하지 않는 작성자")
        void createBoard_UserNotFound() {
            // given
            BoardCreateRequest dto = BoardCreateRequest.builder()
                    .boardType(BoardType.SUGGESTION)
                    .title("제목")
                    .content("내용")
                    .build();

            Long writerId = 1L;
            when(memberRepository.findById(writerId)).thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> boardService.createBoard(dto, null, writerId))
                    .isInstanceOf(CustomException.class)
                    .extracting("errorType")
                    .isEqualTo(ErrorType.USER_NOT_FOUND);
        }

        @Test
        @DisplayName("예외 - 잘못된 게시판 타입")
        void createBoard_ForbiddenForNormalUser() {
            Member member = mock(Member.class);
            when(member.isAdminOrManager()).thenReturn(false);
            when(memberRepository.findById(1L)).thenReturn(Optional.of(member));

            BoardCreateRequest dto = BoardCreateRequest.builder()
                    .boardType(BoardType.NOTICE) // SUGGESTION 아님
                    .title("제목")
                    .content("내용")
                    .build();

            assertThatThrownBy(() -> boardService.createBoard(dto, null, 1L))
                    .isInstanceOf(CustomException.class)
                    .extracting("errorType")
                    .isEqualTo(ErrorType.FORBIDDEN);
        }
    }

    @Nested
    @DisplayName("getBoardList 메서드 테스트")
    class getBoardList {
        SearchParamDto paramDto = SearchParamDto.builder().page(1).size(10).build();
        Pageable pageable = paramDto.getPageable();

        @Test
        @DisplayName("성공 - PageResponseDto 변환")
        void getBoardList_Success() {
            // given
            BoardListResponse dto1 = new BoardListResponse(
                    1L,
                    BoardType.NOTICE,
                    "writer1",
                    "제목1",
                    LocalDateTime.now()
            );
            BoardListResponse dto2 = new BoardListResponse(
                    2L,
                    BoardType.SUGGESTION,
                    "writer2",
                    "제목2",
                    LocalDateTime.now()
            );

            Page<BoardListResponse> page = new org.springframework.data.domain.PageImpl<>(
                    List.of(dto1, dto2), pageable, 2
            );

            when(boardRepository.findBoardList(pageable, paramDto)).thenReturn(page);

            // when
            PageResponseDto<BoardListResponse> response = boardService.getBoardList(paramDto);

            // then
            assertThat(response).isNotNull();
            assertThat(response.getContent()).hasSize(2);
            assertThat(response.getTotalElements()).isEqualTo(2);
            assertThat(response.getTotalPages()).isEqualTo(1);
            assertThat(response.isFirst()).isTrue();
            assertThat(response.isLast()).isTrue();
            assertThat(response.isEmpty()).isFalse();
        }

        @Test
        @DisplayName("성공 - 빈 페이지 반환")
        void getBoardList_Empty() {
            // given
            Page<BoardListResponse> emptyPage = new PageImpl<>(List.of(), pageable, 0);
            when(boardRepository.findBoardList(pageable, paramDto)).thenReturn(emptyPage);

            // when
            PageResponseDto<BoardListResponse> response = boardService.getBoardList(paramDto);

            // then
            assertThat(response).isNotNull();
            assertThat(response.getContent()).isEmpty();
            assertThat(response.getTotalElements()).isEqualTo(0);
            assertThat(response.getTotalPages()).isEqualTo(0);
            assertThat(response.isFirst()).isTrue();
            assertThat(response.isLast()).isTrue();
            assertThat(response.isEmpty()).isTrue();
        }
    }

    @Nested
    @DisplayName("getBoardDetail 메서드 테스트")
    class getBoardDetail {
        @Test
        @DisplayName("성공 - 본인 게시글 조회")
        void getBoardDetail_Success_Owner() {
            // given
            Long boardId = 1L;
            Long currentUserId = 100L;

            Member writer = Member.builder()
                    .memberId(currentUserId)
                    .name("작성자")
                    .build();

            Board board = Board.builder()
                    .boardId(boardId)
                    .boardType(BoardType.NOTICE)
                    .writer(writer)
                    .title("테스트 제목")
                    .content("테스트 내용")
                    .build();

            FileMeta file = FileMeta.builder()
                    .fileId(10L)
                    .originalName("test.png")
                    .filePath("/upload/test.png")
                    .relatedType("board_notice")
                    .relatedId(boardId)
                    .build();

            when(boardRepository.findById(boardId)).thenReturn(Optional.of(board));
            when(fileRepository.findAllByRelatedTypeAndRelatedId("board_notice", boardId))
                    .thenReturn(List.of(file));

            // when
            BoardDetailDto result = boardService.getBoardDetail(boardId, currentUserId);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getBoardId()).isEqualTo(boardId);
            assertThat(result.isOwner()).isTrue();
            assertThat(result.getFiles())
                    .extracting("originalName")
                    .containsExactly("test.png");
        }

        @Test
        @DisplayName("성공 - 타인 게시글 조회")
        void getBoardDetail_Success_NotOwner() {
            // given
            Long boardId = 1L;
            Long writerId = 200L;
            Long currentUserId = 100L;

            Member writer = Member.builder()
                    .memberId(writerId)
                    .name("다른작성자")
                    .build();

            Board board = Board.builder()
                    .boardId(boardId)
                    .boardType(BoardType.NOTICE)
                    .writer(writer)
                    .title("테스트 제목")
                    .content("테스트 내용")
                    .build();

            when(boardRepository.findById(boardId)).thenReturn(Optional.of(board));
            when(fileRepository.findAllByRelatedTypeAndRelatedId("board_notice", boardId))
                    .thenReturn(List.of());

            // when
            BoardDetailDto result = boardService.getBoardDetail(boardId, currentUserId);

            // then
            assertThat(result.isOwner()).isFalse();
            assertThat(result.getFiles()).isEmpty();
        }

        @Test
        @DisplayName("예외 - 게시글이 존재하지 않으면 CustomException 발생")
        void getBoardDetail_BoardNotFound() {
            // given
            Long boardId = 1L;
            Long currentUserId = 100L;

            when(boardRepository.findById(boardId)).thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> boardService.getBoardDetail(boardId, currentUserId))
                    .isInstanceOf(CustomException.class)
                    .extracting("errorType")
                    .isEqualTo(ErrorType.BOARD_NOT_FOUND);
        }
    }

    @Nested
    @DisplayName("softDeleteBoard 메서드 테스트")
    class softDeleteBoard {
        @Test
        @DisplayName("성공 - 정상 게시글 삭제")
        void softDeleteBoard_Success() {
            // given
            Long boardId = 1L;
            Board board = Board.builder()
                    .boardId(boardId)
                    .isDeleted(false)
                    .build();

            when(boardRepository.findById(boardId)).thenReturn(Optional.of(board));

            // when
            boardService.softDeleteBoard(boardId);

            // then
            assertThat(board.getIsDeleted()).isTrue();
        }

        @Test
        @DisplayName("예외 - 게시글이 존재하지 않음")
        void softDeleteBoard_BoardNotFound() {
            // given
            Long boardId = 1L;
            when(boardRepository.findById(boardId)).thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> boardService.softDeleteBoard(boardId))
                    .isInstanceOf(CustomException.class)
                    .extracting("errorType")
                    .isEqualTo(ErrorType.BOARD_NOT_FOUND);
        }

        @Test
        @DisplayName("예외 - 이미 삭제된 게시글")
        void softDeleteBoard_AlreadyDeleted() {
            // given
            Long boardId = 1L;
            Board board = Board.builder()
                    .boardId(boardId)
                    .isDeleted(true)
                    .build();

            when(boardRepository.findById(boardId)).thenReturn(Optional.of(board));

            // when & then
            assertThatThrownBy(() -> boardService.softDeleteBoard(boardId))
                    .isInstanceOf(CustomException.class)
                    .extracting("errorType")
                    .isEqualTo(ErrorType.ALREADY_DELETED);
        }
    }

    @Nested
    @DisplayName("updateBoard 메서드 테스트")
    class updateBoard {
        @Test
        @DisplayName("성공 - 게시글 업데이트 (파일 미첨부)")
        void updateBoard_Success_NoFiles() {
            // given
            Long boardId = 1L;
            Board board = Board.builder()
                    .boardId(boardId)
                    .boardType(BoardType.NOTICE)
                    .title("old title")
                    .content("old content")
                    .build();

            BoardUpdateRequest request = BoardUpdateRequest.builder()
                    .title("new title")
                    .content("new content")
                    .deletedFileIds(List.of())
                    .boardType(BoardType.NOTICE)
                    .build();

            when(boardRepository.findById(boardId)).thenReturn(Optional.of(board));
            when(fileRepository.findAllByRelatedTypeAndRelatedId(anyString(), eq(boardId)))
                    .thenReturn(List.of());

            // when
            BoardUpdateResponse result = boardService.updateBoard(boardId, request, List.of());

            // then
            assertThat(result.getTitle()).isEqualTo("new title");
            assertThat(result.getContent()).isEqualTo("new content");
            assertThat(result.getFiles()).isEmpty();
        }

        @Test
        @DisplayName("성공 - 게시글 업데이트 (기존 파일 제거)")
        void updateBoard_DeleteFiles() {
            // given
            Long boardId = 1L;
            Board board = Board.builder()
                    .boardId(boardId)
                    .boardType(BoardType.NOTICE)
                    .build();

            List<Long> deletedIds = List.of(10L);
            BoardUpdateRequest request = BoardUpdateRequest.builder()
                    .deletedFileIds(deletedIds)
                    .boardType(BoardType.NOTICE)
                    .build();

            FileMeta fileToDelete = FileMeta.builder()
                    .fileId(10L)
                    .filePath("/upload/test.png")
                    .build();

            when(boardRepository.findById(boardId)).thenReturn(Optional.of(board));
            when(fileRepository.findAllById(deletedIds)).thenReturn(List.of(fileToDelete));

            // when
            boardService.updateBoard(boardId, request, List.of());

            // then
            verify(fileRepository).delete(fileToDelete);
            verify(fileService).deleteFile("/upload/test.png", "board_notice");
        }

        @Test
        @DisplayName("성공 - 게시글 업데이트 (신규 파일 추가)")
        void updateBoard_SaveFiles() throws IOException {
            // given
            Long boardId = 1L;
            Board board = Board.builder()
                    .boardId(boardId)
                    .boardType(BoardType.NOTICE)
                    .build();

            BoardUpdateRequest request = BoardUpdateRequest.builder()
                    .boardType(BoardType.NOTICE)
                    .build();

            MultipartFile fileMock = mock(MultipartFile.class);
            when(fileMock.getOriginalFilename()).thenReturn("file1.png");
            when(fileMock.getContentType()).thenReturn("image/png");
            when(fileMock.getSize()).thenReturn(100L);

            List<MultipartFile> files = List.of(fileMock);
            when(boardRepository.findById(boardId)).thenReturn(Optional.of(board));
            when(fileService.saveFiles(files, "board_notice")).thenReturn(List.of("/upload/file1.png"));

            // when
            boardService.updateBoard(boardId, request, files);

            // then
            verify(fileRepository).saveAll(anyList());
        }

        @Test
        @DisplayName("예외 - 게시글 없음")
        void updateBoard_BoardNotFound() {
            // given
            Long boardId = 1L;
            BoardUpdateRequest request = BoardUpdateRequest.builder().build();

            when(boardRepository.findById(boardId)).thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> boardService.updateBoard(boardId, request, List.of()))
                    .isInstanceOf(CustomException.class)
                    .extracting("errorType")
                    .isEqualTo(ErrorType.BOARD_NOT_FOUND);
        }
    }
}
