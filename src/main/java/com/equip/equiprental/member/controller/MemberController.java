package com.equip.equiprental.member.controller;

import com.equip.equiprental.common.response.PageResponseDto;
import com.equip.equiprental.common.response.ResponseController;
import com.equip.equiprental.common.response.ResponseDto;
import com.equip.equiprental.common.response.SearchParamDto;
import com.equip.equiprental.member.dto.*;
import com.equip.equiprental.common.interceptor.RequestTraceIdInterceptor;
import com.equip.equiprental.member.service.MemberService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/members")
public class MemberController implements ResponseController {
    private final MemberService memberService;

    @PostMapping("")
    public ResponseEntity<ResponseDto<SignUpResponseDto>> createMember(@RequestBody SignUpRequestDto dto) {
        String traceId = RequestTraceIdInterceptor.getTraceId();
        log.info("[회원 가입 요청 API] TraceId={}", traceId);

        SignUpResponseDto result = memberService.signUp(dto);
        return makeResponseEntity(traceId, HttpStatus.OK, null, "사용자 가입 성공", result);
    }

    @GetMapping("")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResponseDto<PageResponseDto<MemberDto>>> searchMembers(@ModelAttribute SearchParamDto dto) {
        String traceId = RequestTraceIdInterceptor.getTraceId();
        log.info("[사용자 목록 조회 요청 API] TraceId={}", traceId);

        PageResponseDto<MemberDto> result = memberService.searchMembers(dto);
        return makeResponseEntity(traceId, HttpStatus.OK, null, "사용자 목록 조회 성공", result);
    }

    @PatchMapping("/{memberId}/status")
    public ResponseEntity<ResponseDto<MemberStatusDto>> updateStatus(@PathVariable Long memberId, @RequestBody UpdateMemberRequestDto dto){
        String traceId = RequestTraceIdInterceptor.getTraceId();
        log.info("[사용자 상태 변경 요청 API] TraceId={}", traceId);

        MemberStatusDto result = memberService.updateMemberStatus(memberId, dto);
        return makeResponseEntity(traceId, HttpStatus.OK, null, "사용자 상태 변경 성공", result);
    }

    @PatchMapping("/{memberId}/role")
    public ResponseEntity<ResponseDto<MemberRoleDto>> updateRole(@PathVariable Long memberId, @RequestBody UpdateMemberRequestDto dto){
        String traceId = RequestTraceIdInterceptor.getTraceId();
        log.info("[사용자 역할 변경 요청 API] TraceId={}", traceId);

        MemberRoleDto result = memberService.updateMemberRole(memberId, dto);
        return makeResponseEntity(traceId, HttpStatus.OK, null, "사용자 역할 변경 성공", result);
    }
}
