package com.equip.equiprental.scope.controller;

import com.equip.equiprental.auth.security.PrincipalDetails;
import com.equip.equiprental.common.controller.ResponseController;
import com.equip.equiprental.common.dto.ResponseDto;
import com.equip.equiprental.common.interceptor.RequestTraceIdInterceptor;
import com.equip.equiprental.scope.dto.ManagerScopeRequest;
import com.equip.equiprental.scope.service.ManagerScopeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/manager-scopes")
public class ManagerScopeController implements ResponseController {
    private final ManagerScopeService managerScopeService;

    @PostMapping("")
    public ResponseEntity<ResponseDto<Void>> setManagerScope(@RequestBody ManagerScopeRequest dto) {
        String traceId = RequestTraceIdInterceptor.getTraceId();
        log.info("[매니저 스코프 변경 요청 API] TraceId={}", traceId);

        managerScopeService.setScope(dto.getManagerId(), dto.getCategoryIds());
        return makeResponseEntity(traceId, HttpStatus.OK, null, "접근 범위가 업데이트되었습니다.", null);
    }

    @GetMapping("/{equipmentId}")
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
