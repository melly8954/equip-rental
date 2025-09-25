package com.equip.equiprental.board.controller;

import com.equip.equiprental.auth.security.PrincipalDetails;
import com.equip.equiprental.board.dto.CommentCreateRequest;
import com.equip.equiprental.board.dto.CommentCreateResponse;
import com.equip.equiprental.board.dto.CommentListResponse;
import com.equip.equiprental.board.service.CommentService;
import com.equip.equiprental.common.controller.ResponseController;
import com.equip.equiprental.common.dto.PageResponseDto;
import com.equip.equiprental.common.dto.ResponseDto;
import com.equip.equiprental.common.dto.SearchParamDto;
import com.equip.equiprental.common.interceptor.RequestTraceIdInterceptor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/comments")
public class CommentController implements ResponseController {
    private final CommentService commentService;

    @PostMapping("")
    public ResponseEntity<ResponseDto<CommentCreateResponse>> createComment(@RequestBody CommentCreateRequest dto,
                                                                            @AuthenticationPrincipal PrincipalDetails principal) {
        String traceId = RequestTraceIdInterceptor.getTraceId();
        log.info("댓글 등록 요청 API] TraceId={}", traceId);

        Long writerId = principal.getMember().getMemberId();

        CommentCreateResponse result = commentService.createComment(dto, writerId);

        return makeResponseEntity(traceId, HttpStatus.OK, null, "댓글 등록 성공", result);
    }

    @GetMapping("")
    public ResponseEntity<ResponseDto<PageResponseDto<CommentListResponse>>> getCommentList(@ModelAttribute SearchParamDto paramDto,
                                                                                            @AuthenticationPrincipal PrincipalDetails principal) {
        String traceId = RequestTraceIdInterceptor.getTraceId();
        log.info("댓글 조회 요청 API] TraceId={}", traceId);

        Long writerId = principal.getMember().getMemberId();

        PageResponseDto<CommentListResponse> result = commentService.getCommentList(paramDto, writerId);

        return makeResponseEntity(traceId, HttpStatus.OK, null, "댓글 조회 성공", result);
    }
}
