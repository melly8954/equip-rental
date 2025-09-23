package com.equip.equiprental.board.controller;

import com.equip.equiprental.auth.security.PrincipalDetails;
import com.equip.equiprental.board.dto.BoardCreateRequest;
import com.equip.equiprental.board.dto.BoardCreateResponse;
import com.equip.equiprental.board.service.BoardService;
import com.equip.equiprental.common.controller.ResponseController;
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
        log.info("게시글 추가 요청 API] TraceId={}", traceId);

        Long writerId = principal.getMember().getMemberId();

        BoardCreateResponse result = boardService.createBoard(boardCreateRequest, files, writerId);

        return makeResponseEntity(traceId, HttpStatus.OK, null, "게시글 추가 요청 성공", result);
    }
}
