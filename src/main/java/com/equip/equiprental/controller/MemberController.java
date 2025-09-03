package com.equip.equiprental.controller;

import com.equip.equiprental.common.controller.ResponseController;
import com.equip.equiprental.common.dto.ResponseDto;
import com.equip.equiprental.dto.member.SignUpRequestDto;
import com.equip.equiprental.dto.member.SignUpResponseDto;
import com.equip.equiprental.common.interceptor.RequestTraceIdInterceptor;
import com.equip.equiprental.service.MemberService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}
