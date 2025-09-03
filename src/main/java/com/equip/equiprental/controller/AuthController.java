package com.equip.equiprental.controller;

import com.equip.equiprental.common.controller.ResponseController;
import com.equip.equiprental.common.dto.ResponseDto;
import com.equip.equiprental.dto.auth.LoginRequestDto;
import com.equip.equiprental.dto.auth.LoginResponseDto;
import com.equip.equiprental.interceptor.RequestTraceIdInterceptor;
import com.equip.equiprental.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
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
@RequestMapping("/api/v1/auth")
public class AuthController implements ResponseController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<ResponseDto<LoginResponseDto>> login(@RequestBody LoginRequestDto dto,
                                                               HttpServletRequest httpRequest) {
        String traceId = RequestTraceIdInterceptor.getTraceId();
        log.info("[로그인 요청 API] TraceId={}", traceId);

        LoginResponseDto result = authService.login(httpRequest, dto);
        return makeResponseEntity(traceId, HttpStatus.OK, null, "로그인 성공", result);
    }

}
