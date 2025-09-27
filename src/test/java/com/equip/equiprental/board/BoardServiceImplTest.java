package com.equip.equiprental.board;


import com.equip.equiprental.board.domain.Board;
import com.equip.equiprental.board.domain.BoardStatus;
import com.equip.equiprental.board.domain.BoardType;
import com.equip.equiprental.board.dto.BoardCreateRequest;
import com.equip.equiprental.board.dto.BoardCreateResponse;
import com.equip.equiprental.board.dto.BoardListResponse;
import com.equip.equiprental.board.repository.BoardRepository;
import com.equip.equiprental.board.service.BoardServiceImpl;
import com.equip.equiprental.common.dto.PageResponseDto;
import com.equip.equiprental.common.dto.SearchParamDto;
import com.equip.equiprental.common.exception.CustomException;
import com.equip.equiprental.common.exception.ErrorType;
import com.equip.equiprental.filestorage.repository.FileRepository;
import com.equip.equiprental.filestorage.service.iface.FileService;
import com.equip.equiprental.member.domain.Member;
import com.equip.equiprental.member.repository.MemberRepository;
import com.equip.equiprental.rental.dto.AdminRentalDto;
import org.junit.jupiter.api.BeforeEach;
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
    private BoardServiceImpl boardServiceImpl;

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

            BoardCreateResponse response = boardServiceImpl.createBoard(dto, files, 1L);

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

            BoardCreateResponse response = boardServiceImpl.createBoard(dto, null, 1L);

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
            assertThatThrownBy(() -> boardServiceImpl.createBoard(dto, null, writerId))
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

            assertThatThrownBy(() -> boardServiceImpl.createBoard(dto, null, 1L))
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
            PageResponseDto<BoardListResponse> response = boardServiceImpl.getBoardList(paramDto);

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
            PageResponseDto<BoardListResponse> response = boardServiceImpl.getBoardList(paramDto);

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
}
