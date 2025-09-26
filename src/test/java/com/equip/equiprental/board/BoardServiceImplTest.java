package com.equip.equiprental.board;


import com.equip.equiprental.board.domain.Board;
import com.equip.equiprental.board.domain.BoardStatus;
import com.equip.equiprental.board.domain.BoardType;
import com.equip.equiprental.board.dto.BoardCreateRequest;
import com.equip.equiprental.board.dto.BoardCreateResponse;
import com.equip.equiprental.board.repository.BoardRepository;
import com.equip.equiprental.board.service.BoardServiceImpl;
import com.equip.equiprental.common.exception.CustomException;
import com.equip.equiprental.common.exception.ErrorType;
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
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
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

}
