package com.equip.equiprental.dashboard.controller;

import com.equip.equiprental.common.controller.ResponseController;
import com.equip.equiprental.common.dto.ResponseDto;
import com.equip.equiprental.common.interceptor.RequestTraceIdInterceptor;
import com.equip.equiprental.dashboard.dto.KpiResponseDto;
import com.equip.equiprental.dashboard.service.DashBoardService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/dashboards")
public class DashBoardController implements ResponseController {
    private final DashBoardService dashBoardService;

    @GetMapping("/kpi")
    @PreAuthorize("hasRole('ADMIN') or (hasRole('MANAGER'))")
    public ResponseEntity<ResponseDto<KpiResponseDto>> getDashBoardKpi() {
        String traceId = RequestTraceIdInterceptor.getTraceId();
        log.info("대쉬보드 Kpi 조회 요청 API] TraceId={}", traceId);

        KpiResponseDto result = dashBoardService.getDashBoardKpi();

        return makeResponseEntity(traceId, HttpStatus.OK, null, "대쉬보드 Kpi 조회 성공", result);
    }
}
