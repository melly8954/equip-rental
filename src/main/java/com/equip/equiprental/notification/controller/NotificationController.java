package com.equip.equiprental.notification.controller;

import com.equip.equiprental.auth.security.PrincipalDetails;
import com.equip.equiprental.common.controller.ResponseController;
import com.equip.equiprental.common.dto.PageResponseDto;
import com.equip.equiprental.common.dto.ResponseDto;
import com.equip.equiprental.common.interceptor.RequestTraceIdInterceptor;
import com.equip.equiprental.notification.dto.NotificationDto;
import com.equip.equiprental.notification.dto.NotificationFilter;
import com.equip.equiprental.notification.dto.ReadRequestDto;
import com.equip.equiprental.notification.dto.UnreadCountResponseDto;
import com.equip.equiprental.notification.service.iface.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/notifications")
public class NotificationController implements ResponseController {
    private final NotificationService notificationService;

    @GetMapping("/unread-count")
    public ResponseEntity<ResponseDto<UnreadCountResponseDto>> getUnreadCount(@AuthenticationPrincipal PrincipalDetails principal) {
        String traceId = RequestTraceIdInterceptor.getTraceId();
        log.info("[읽지 않은 알림 수 조회 요청 API] TraceId={}", traceId);

        Long memberId = principal.getMember().getMemberId();

        UnreadCountResponseDto result = notificationService.getUnreadCount(memberId);

        return makeResponseEntity(traceId, HttpStatus.OK, null, "읽지 않은 알림 수 조회 성공", result);
    }

    @GetMapping("")
    public ResponseEntity<ResponseDto<PageResponseDto<NotificationDto>>> getUnreadNotifications(@ModelAttribute NotificationFilter paramDto,
                                                                                                @AuthenticationPrincipal PrincipalDetails principal) {
        String traceId = RequestTraceIdInterceptor.getTraceId();
        log.info("[알림 조회 요청 API] TraceId={}", traceId);

        Long memberId = principal.getMember().getMemberId();

        PageResponseDto<NotificationDto> result = notificationService.getNotificationList(paramDto, memberId);

        return makeResponseEntity(traceId, HttpStatus.OK, null, "알림 조회 성공", result);
    }

    @PatchMapping("/{notificationId}")
    public ResponseEntity<ResponseDto<Void>> updateNotificationStatus(@PathVariable Long notificationId,
                                                                      @RequestBody ReadRequestDto dto) {
        String traceId = RequestTraceIdInterceptor.getTraceId();
        log.info("[알림 읽음 처리 요청 API] TraceId={}", traceId);

        notificationService.updateNotificationStatus(notificationId, dto);

        return makeResponseEntity(traceId, HttpStatus.OK, null, "알림 읽음 처리 성공", null);
    }
}
