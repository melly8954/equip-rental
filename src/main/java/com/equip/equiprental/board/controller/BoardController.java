package com.equip.equiprental.board.controller;

import com.equip.equiprental.auth.security.PrincipalDetails;
import com.equip.equiprental.board.dto.*;
import com.equip.equiprental.board.service.iface.BoardService;
import com.equip.equiprental.common.controller.ResponseController;
import com.equip.equiprental.common.dto.PageResponseDto;
import com.equip.equiprental.common.dto.ResponseDto;
import com.equip.equiprental.common.interceptor.RequestTraceIdInterceptor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/boards")
public class BoardController implements ResponseController {
    private final BoardService boardService;

    @PostMapping("")
    public ResponseEntity<ResponseDto<BoardCreateResponse>> createBoard(@RequestPart(value = "data") BoardCreateRequest boardCreateRequest,
                                                                        @RequestPart(value = "files", required = false) List<MultipartFile> files,
                                                                        @AuthenticationPrincipal PrincipalDetails principal) {
        String traceId = RequestTraceIdInterceptor.getTraceId();
        log.info("[게시글 등록 요청 API] TraceId={}", traceId);

        Long writerId = principal.getMember().getMemberId();

        BoardCreateResponse result = boardService.createBoard(boardCreateRequest, files, writerId);

        return makeResponseEntity(traceId, HttpStatus.OK, null, "게시글 등록 요청 성공", result);
    }

    @GetMapping("")
    public ResponseEntity<ResponseDto<PageResponseDto<BoardListResponse>>> getBoardList(@ModelAttribute BoardFilter paramDto) {
        String traceId = RequestTraceIdInterceptor.getTraceId();
        log.info("[게시글 목록 조회 요청 API] TraceId={}", traceId);

        PageResponseDto<BoardListResponse> result = boardService.getBoardList(paramDto);

        return makeResponseEntity(traceId, HttpStatus.OK, null, "게시글 목록 조회 성공", result);
    }

    @GetMapping("/notices")
    public ResponseEntity<ResponseDto<List<BoardListResponse>>> getNotices() {
        String traceId = RequestTraceIdInterceptor.getTraceId();
        log.info("[공지글 최신 5개 조회 요청 API] TraceId={}", traceId);

        List<BoardListResponse> result = boardService.getLatestNotices(5);

        return makeResponseEntity(traceId, HttpStatus.OK, null, "공지글 조회 성공", result);
    }

    @GetMapping("/{boardId}")
    public ResponseEntity<ResponseDto<BoardDetailDto>> getBoardDetail(@PathVariable Long boardId,
                                                                      @AuthenticationPrincipal PrincipalDetails principal) {
        String traceId = RequestTraceIdInterceptor.getTraceId();
        log.info("[게시글 상세 조회 요청 API] TraceId={}", traceId);

        Long currentUserId = principal.getMember().getMemberId();
        BoardDetailDto result = boardService.getBoardDetail(boardId, currentUserId);

        return makeResponseEntity(traceId, HttpStatus.OK, null, "게시글 상세 조회 성공", result);
    }

    @DeleteMapping("/{boardId}")
    public ResponseEntity<ResponseDto<Void>> softDeleteBoard(@PathVariable Long boardId) {
        String traceId = RequestTraceIdInterceptor.getTraceId();
        log.info("[게시글 논리 삭제 요청 API] TraceId={}", traceId);

        boardService.softDeleteBoard(boardId);
        return makeResponseEntity(traceId, HttpStatus.OK, null, "게시글 논리 삭제 성공", null);
    }

    @PatchMapping("/{boardId}")
    public ResponseEntity<ResponseDto<BoardUpdateResponse>> updateBoard(@PathVariable Long boardId,
                                                                        @RequestPart(value = "data") BoardUpdateRequest boardCreateRequest,
                                                                        @RequestPart(value = "files", required = false) List<MultipartFile> files) {
        String traceId = RequestTraceIdInterceptor.getTraceId();
        log.info("[게시글 수정 요청 API] TraceId={}", traceId);

        BoardUpdateResponse result = boardService.updateBoard(boardId, boardCreateRequest, files);

        return makeResponseEntity(traceId, HttpStatus.OK, null, "게시글 수정 성공", result);
    }
}
