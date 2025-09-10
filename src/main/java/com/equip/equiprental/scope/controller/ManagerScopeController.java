package com.equip.equiprental.scope.controller;

import com.equip.equiprental.auth.security.PrincipalDetails;
import com.equip.equiprental.common.controller.ResponseController;
import com.equip.equiprental.common.dto.ResponseDto;
import com.equip.equiprental.common.interceptor.RequestTraceIdInterceptor;
import com.equip.equiprental.scope.service.ManagerScopeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class ManagerScopeController implements ResponseController {
    private final ManagerScopeService managerScopeService;

    @GetMapping("/manager-scopes/{equipmentId}")
    @PreAuthorize("hasRole('ADMIN') or (hasRole('MANAGER'))")
    public ResponseEntity<ResponseDto<Boolean>> checkManagerScope(@PathVariable Long equipmentId,
                                                                  @AuthenticationPrincipal PrincipalDetails principal) {
        String traceId = RequestTraceIdInterceptor.getTraceId();
        log.info("[매니저 스코프 확인 요청 API] TraceId={}", traceId);

        boolean canAccess = false;

        String role = principal.getMember().getRole().name();
        if ("ADMIN".equalsIgnoreCase(role)) {
            canAccess = true;
        } else if ("MANAGER".equalsIgnoreCase(role)) {
            canAccess = managerScopeService.canAccessEquipment(
                    equipmentId, principal.getMember().getMemberId());
        }

        return makeResponseEntity(traceId, HttpStatus.OK, null, "접근 가능 여부 조회 성공", canAccess);
    }
}
