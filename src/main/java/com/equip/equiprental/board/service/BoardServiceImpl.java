package com.equip.equiprental.board.service;

import com.equip.equiprental.board.domain.Board;
import com.equip.equiprental.board.domain.BoardStatus;
import com.equip.equiprental.board.domain.BoardType;
import com.equip.equiprental.board.dto.*;
import com.equip.equiprental.board.repository.BoardRepository;
import com.equip.equiprental.common.dto.PageResponseDto;
import com.equip.equiprental.common.dto.SearchParamDto;
import com.equip.equiprental.common.exception.CustomException;
import com.equip.equiprental.common.exception.ErrorType;
import com.equip.equiprental.filestorage.domain.FileMeta;
import com.equip.equiprental.filestorage.repository.FileRepository;
import com.equip.equiprental.filestorage.service.FileService;
import com.equip.equiprental.member.domain.Member;
import com.equip.equiprental.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BoardServiceImpl implements BoardService {
    private final BoardRepository boardRepository;
    private final MemberRepository memberRepository;
    private final FileRepository fileRepository;
    private final FileService fileService;

    @Override
    @Transactional
    public BoardCreateResponse createBoard(BoardCreateRequest dto, List<MultipartFile> files, Long writerId) {
        Member writer = memberRepository.findById(writerId)
                .orElseThrow(() -> new CustomException(ErrorType.USER_NOT_FOUND));

        BoardType boardType = dto.getBoardType();
        if (!writer.isAdminOrManager() && boardType != BoardType.SUGGESTION) {
            throw new CustomException(ErrorType.FORBIDDEN);
        }

        Board board = Board.builder()
                .writer(writer)
                .boardType(boardType)
                .title(dto.getTitle())
                .content(dto.getContent())
                .status(BoardStatus.PENDING)
                .isDeleted(false)
                .build();
        boardRepository.save(board);

        List<FileMeta> savedFiles = new ArrayList<>();

        if(files != null && !files.isEmpty()) {
            int fileOrder = 0;
            String typeKey = "board_" + boardType.name().toLowerCase();
            List<String> fileUrls = fileService.saveFiles(files, typeKey);

            for (int i = 0; i < files.size(); i++) {
                MultipartFile file = files.get(i);
                String url = fileUrls.get(i); // fileService에서 생성한 접근 URL

                FileMeta meta = FileMeta.builder()
                        .relatedType(typeKey)
                        .relatedId(board.getBoardId())
                        .originalName(file.getOriginalFilename())
                        .uniqueName(url.substring(url.lastIndexOf("/") + 1)) // URL 에서 uniqueName 추출
                        .fileOrder(fileOrder++)
                        .fileType(file.getContentType())
                        .filePath(url) // 접근 URL
                        .fileSize(file.getSize())
                        .build();

                savedFiles.add(meta);
            }

            fileRepository.saveAll(savedFiles);
        }

        return BoardCreateResponse.builder()
                .boardId(board.getBoardId())
                .boardType(board.getBoardType().name())
                .title(board.getTitle())
                .content(board.getContent())
                .status(board.getStatus().name())
                .createdAt(board.getCreatedAt())
                .files(savedFiles.stream()
                        .map(f -> BoardCreateResponse.BoardFile.builder()
                                .fileId(f.getFileId())
                                .originalName(f.getOriginalName())
                                .url(f.getFilePath())
                                .fileOrder(f.getFileOrder())
                                .fileType(f.getFileType())
                                .fileSize(f.getFileSize())
                                .build())
                        .toList())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponseDto<BoardListResponse> getBoardList(SearchParamDto paramDto) {
        Pageable pageable = paramDto.getPageable();

        Page<BoardListResponse> dtosPage = boardRepository.findBoardList(pageable, paramDto.getBoardType());

        return PageResponseDto.<BoardListResponse>builder()
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
    @Transactional(readOnly = true)
    public List<BoardListResponse> getLatestNotices(int limit) {
        return boardRepository.findLatestNotices(limit);
    }

    @Override
    @Transactional(readOnly = true)
    public BoardDetailDto getBoardDetail(Long boardId, Long currentUserId) {
        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new CustomException(ErrorType.BOARD_NOT_FOUND));

        boolean isOwner = board.getWriter().getMemberId().equals(currentUserId);

        String relatedType = "board_" + board.getBoardType().name().toLowerCase();
        List<BoardFileDto> files = fileRepository.findAllByRelatedTypeAndRelatedId(relatedType, boardId)
                .stream()
                .map(file -> BoardFileDto.builder()
                        .fileId(file.getFileId())
                        .originalName(file.getOriginalName())
                        .filePath(file.getFilePath())
                        .build())
                .toList();

        return BoardDetailDto.builder()
                .boardId(boardId)
                .boardType(board.getBoardType())
                .title(board.getTitle())
                .content(board.getContent())
                .createdAt(board.getCreatedAt())
                .files(files)
                .owner(isOwner)
                .build();
    }

    @Override
    @Transactional
    public void softDeleteBoard(Long boardId) {
        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new CustomException(ErrorType.BOARD_NOT_FOUND));

        if (board.getIsDeleted()) {
            throw new CustomException(ErrorType.ALREADY_DELETED);
        }

        board.softDelete();
    }

    @Override
    @Transactional
    public BoardUpdateResponse updateBoard(Long boardId, BoardUpdateRequest boardCreateRequest, List<MultipartFile> files) {
        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new CustomException(ErrorType.BOARD_NOT_FOUND));

        board.updateBoard(boardCreateRequest);

        List<FileMeta> savedFiles = new ArrayList<>();

        if(files != null && !files.isEmpty()) {
            int fileOrder = 0;
            String typeKey = "board_" + boardCreateRequest.getBoardType().name().toLowerCase();
            List<String> fileUrls = fileService.saveFiles(files, typeKey);

            for (int i = 0; i < files.size(); i++) {
                MultipartFile file = files.get(i);
                String url = fileUrls.get(i); // fileService에서 생성한 접근 URL

                FileMeta meta = FileMeta.builder()
                        .relatedType(typeKey)
                        .relatedId(board.getBoardId())
                        .originalName(file.getOriginalFilename())
                        .uniqueName(url.substring(url.lastIndexOf("/") + 1)) // URL 에서 uniqueName 추출
                        .fileOrder(fileOrder++)
                        .fileType(file.getContentType())
                        .filePath(url) // 접근 URL
                        .fileSize(file.getSize())
                        .build();

                savedFiles.add(meta);
            }

            fileRepository.saveAll(savedFiles);
        }

        String relatedType = "board_" + board.getBoardType().name().toLowerCase();
        List<String> paths = fileRepository.findAllByRelatedTypeAndRelatedId(relatedType, boardId)
                .stream()
                .map(FileMeta::getFilePath)
                .toList();

        return BoardUpdateResponse.builder()
                .boardId(board.getBoardId())
                .boardType(board.getBoardType())
                .title(board.getTitle())
                .content(board.getContent())
                .filePaths(paths)
                .build();
    }
}
