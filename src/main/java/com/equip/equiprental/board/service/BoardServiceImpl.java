package com.equip.equiprental.board.service;

import com.equip.equiprental.board.domain.Board;
import com.equip.equiprental.board.domain.BoardStatus;
import com.equip.equiprental.board.domain.BoardType;
import com.equip.equiprental.board.dto.*;
import com.equip.equiprental.board.repository.BoardRepository;
import com.equip.equiprental.board.service.iface.BoardService;
import com.equip.equiprental.common.dto.PageResponseDto;
import com.equip.equiprental.common.exception.CustomException;
import com.equip.equiprental.common.exception.ErrorType;
import com.equip.equiprental.filestorage.domain.FileMeta;
import com.equip.equiprental.filestorage.repository.FileRepository;
import com.equip.equiprental.filestorage.service.iface.FileService;
import com.equip.equiprental.member.domain.Member;
import com.equip.equiprental.member.domain.MemberRole;
import com.equip.equiprental.member.repository.MemberRepository;
import com.equip.equiprental.notification.domain.NotificationType;
import com.equip.equiprental.notification.service.iface.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class BoardServiceImpl implements BoardService {
    private final BoardRepository boardRepository;
    private final MemberRepository memberRepository;
    private final FileRepository fileRepository;
    private final FileService fileService;
    private final NotificationService notificationService;

    @Override
    @Transactional
    public BoardCreateResponse createBoard(BoardCreateRequest dto, List<MultipartFile> files, Long currentUserId) {
        Member writer = memberRepository.findById(currentUserId)
                .orElseThrow(() -> new CustomException(ErrorType.NOT_FOUND, "회원 정보를 찾을 수 없습니다."));

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
        // 알림 처리
        if (boardType == BoardType.NOTICE && writer.isAdminOrManager()) {
            List<Member> allUsers = memberRepository.findAll();
            for (Member user : allUsers) {
                notificationService.createNotification(
                        user,
                        NotificationType.SYSTEM_ANNOUNCEMENT,
                        "새로운 공지사항 추가: " + board.getTitle(),
                        null
                );
            }
        } else if (boardType == BoardType.SUGGESTION) {
            // 문의글 → Admin + Manager
            List<Member> admins = memberRepository.findByRole(MemberRole.ADMIN);
            List<Member> managers = memberRepository.findByRole(MemberRole.MANAGER);
            Set<Member> recipients = new HashSet<>();
            recipients.addAll(admins);
            recipients.addAll(managers);

            for (Member recipient : recipients) {
                notificationService.createNotification(
                        recipient,
                        NotificationType.SUGGESTION_CREATED,
                        writer.getName() + "님이 새로운 문의글을 등록했습니다: " + board.getTitle(),
                        null
                );
            }
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
    public PageResponseDto<BoardListResponse> getBoardList(BoardFilter paramDto) {
        Pageable pageable = paramDto.getPageable();

        Page<BoardListResponse> dtosPage = boardRepository.findBoardList(pageable, paramDto);

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
                .orElseThrow(() -> new CustomException(ErrorType.NOT_FOUND, "해당 게시글은 존재하지 않습니다."));

        Member writer = memberRepository.findById(currentUserId)
                .orElseThrow(() -> new CustomException(ErrorType.NOT_FOUND, "회원 정보를 찾을 수 없습니다."));

        boolean isOwner = board.getWriter().getMemberId().equals(currentUserId);
        boolean isAdmin = writer.isAdmin();

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
                .updatedAt(board.getUpdatedAt())
                .files(files)
                .owner(isOwner)
                .admin(isAdmin)
                .build();
    }

    @Override
    @Transactional
    public void softDeleteBoard(Long boardId) {
        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new CustomException(ErrorType.NOT_FOUND, "해당 게시글은 존재하지 않습니다."));

        if (board.getIsDeleted()) {
            throw new CustomException(ErrorType.CONFLICT, "이미 삭제된 게시글입니다.");
        }

        board.softDelete();
    }

    @Override
    @Transactional
    public BoardUpdateResponse updateBoard(Long boardId, BoardUpdateRequest request, List<MultipartFile> files) {
        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new CustomException(ErrorType.NOT_FOUND, "해당 게시글은 존재하지 않습니다."));

        board.updateBoard(request);

        // 파일 제거 처리
        if (request.getDeletedFileIds() != null && !request.getDeletedFileIds().isEmpty()) {
            List<FileMeta> filesToDelete = fileRepository.findAllById(request.getDeletedFileIds());

            String typeKey = "board_" + board.getBoardType().name().toLowerCase();

            filesToDelete.forEach(file -> {
                fileRepository.delete(file); // DB 삭제
                fileService.deleteFile(file.getFilePath(), typeKey); // 실제 저장소 파일 삭제
            });
        }

        List<FileMeta> savedFiles = new ArrayList<>();

        if(files != null && !files.isEmpty()) {
            int fileOrder = 0;
            String typeKey = "board_" + request.getBoardType().name().toLowerCase();
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
        List<BoardFileDto> dto = fileRepository.findAllByRelatedTypeAndRelatedId(relatedType, boardId)
                .stream()
                .map(file -> BoardFileDto.builder()
                        .fileId(file.getFileId())
                        .originalName(file.getOriginalName())
                        .filePath(file.getFilePath())
                        .build())
                .toList();

        return BoardUpdateResponse.builder()
                .boardId(board.getBoardId())
                .boardType(board.getBoardType())
                .title(board.getTitle())
                .content(board.getContent())
                .files(dto)
                .build();
    }
}
