package com.equip.equiprental.dashboard.controller;

import com.equip.equiprental.common.controller.ResponseController;
import com.equip.equiprental.common.dto.PageResponseDto;
import com.equip.equiprental.common.dto.ResponseDto;
import com.equip.equiprental.common.dto.SearchParamDto;
import com.equip.equiprental.common.interceptor.RequestTraceIdInterceptor;
import com.equip.equiprental.dashboard.dto.*;
import com.equip.equiprental.dashboard.service.DashBoardService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
        log.info("대시보드 Kpi 조회 요청 API] TraceId={}", traceId);

        KpiResponseDto result = dashBoardService.getDashBoardKpi();

        return makeResponseEntity(traceId, HttpStatus.OK, null, "대쉬보드 Kpi 조회 성공", result);
    }

    @GetMapping("/zero-stock")
    @PreAuthorize("hasRole('ADMIN') or (hasRole('MANAGER'))")
    public ResponseEntity<ResponseDto<PageResponseDto<ZeroStockDto>>> getDashBoardZeroStock(@ModelAttribute SearchParamDto paramDto) {
        String traceId = RequestTraceIdInterceptor.getTraceId();
        log.info("긴급 재고 조회 요청 API] TraceId={}", traceId);

        PageResponseDto<ZeroStockDto> result = dashBoardService.getDashBoardZeroStock(paramDto);

        return makeResponseEntity(traceId, HttpStatus.OK, null, "긴급 재고 조회 성공", result);
    }

    @GetMapping("/equipments/category")
    @PreAuthorize("hasRole('ADMIN') or (hasRole('MANAGER'))")
    public ResponseEntity<ResponseDto<List<CategoryInventoryResponse>>> getCategoryInventory() {
        String traceId = RequestTraceIdInterceptor.getTraceId();
        log.info("카테고리 별 기자재 보유 현황 API] TraceId={}", traceId);

        List<CategoryInventoryResponse> result = dashBoardService.getCategoryInventory();

        return makeResponseEntity(traceId, HttpStatus.OK, null, "카테고리 별 기자재 보유 현황 조회 성공", result);
    }

    @GetMapping("/equipments/categories/{categoryId}")
    @PreAuthorize("hasRole('ADMIN') or (hasRole('MANAGER'))")
    public ResponseEntity<ResponseDto<List<SubCategoryInventoryResponse>>> getCategoryInventory(@PathVariable Long categoryId) {
        String traceId = RequestTraceIdInterceptor.getTraceId();
        log.info("서브 카테고리 별 기자재 보유 현황 API] TraceId={}", traceId);

        List<SubCategoryInventoryResponse> result = dashBoardService.getSubCategoryInventory(categoryId);

        return makeResponseEntity(traceId, HttpStatus.OK, null, "서브 카테고리 별 기자재 보유 현황 조회 성공", result);
    }

    @GetMapping("/equipments/{subCategoryId}")
    @PreAuthorize("hasRole('ADMIN') or (hasRole('MANAGER'))")
    public ResponseEntity<ResponseDto<PageResponseDto<InventoryDetail>>> getInventoryDetail(@PathVariable Long subCategoryId,
                                                                                            @ModelAttribute SearchParamDto paramDto) {
        String traceId = RequestTraceIdInterceptor.getTraceId();
        log.info("서브 카테고리 별 기자재 보유 상세 현황 API] TraceId={}", traceId);

        PageResponseDto<InventoryDetail> result = dashBoardService.getInventoryDetail(subCategoryId, paramDto);

        return makeResponseEntity(traceId, HttpStatus.OK, null, "서브 카테고리 별 기자재 보유 상세 현황 조회 성공", result);
    }
}
