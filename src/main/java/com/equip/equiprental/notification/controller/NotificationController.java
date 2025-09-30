package com.equip.equiprental.notification.controller;

import com.equip.equiprental.auth.security.PrincipalDetails;
import com.equip.equiprental.common.controller.ResponseController;
import com.equip.equiprental.common.dto.ResponseDto;
import com.equip.equiprental.common.interceptor.RequestTraceIdInterceptor;
import com.equip.equiprental.notification.dto.NotificationDto;
import com.equip.equiprental.notification.service.iface.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/notifications")
public class NotificationController implements ResponseController {
    private final NotificationService notificationService;

    @GetMapping("")
    public ResponseEntity<ResponseDto<List<NotificationDto>>> getUnreadNotifications(@AuthenticationPrincipal PrincipalDetails principal) {
        String traceId = RequestTraceIdInterceptor.getTraceId();
        log.info("[알림 조회 요청 API] TraceId={}", traceId);

        List<NotificationDto> result = notificationService.getUnreadNotifications(principal.getMember().getMemberId());

        return makeResponseEntity(traceId, HttpStatus.OK, null, "알림 조회 성공", result);
    }
}
