package com.equip.equiprental.rental.controller;

import com.equip.equiprental.auth.security.PrincipalDetails;
import com.equip.equiprental.common.controller.ResponseController;
import com.equip.equiprental.common.dto.PageResponseDto;
import com.equip.equiprental.common.dto.ResponseDto;
import com.equip.equiprental.common.interceptor.RequestTraceIdInterceptor;
import com.equip.equiprental.rental.dto.AdminRentalItemDto;
import com.equip.equiprental.rental.dto.ExtendRentalItemDto;
import com.equip.equiprental.rental.dto.RentalFilter;
import com.equip.equiprental.rental.service.iface.RentalItemService;
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
@RequestMapping("/api/v1/rental-items")
public class RentalItemController implements ResponseController {

    private final RentalItemService rentalItemService;

    @GetMapping("")
    @PreAuthorize("hasRole('ADMIN') or (hasRole('MANAGER'))")
    public ResponseEntity<ResponseDto<PageResponseDto<AdminRentalItemDto>>> getAdminRentalItemList(@ModelAttribute RentalFilter paramDto){
        String traceId = RequestTraceIdInterceptor.getTraceId();
        log.info("관리자 대여 물품 내역 조회 요청 API] TraceId={}", traceId);

        PageResponseDto<AdminRentalItemDto> result = rentalItemService.getAdminRentalItemLists(paramDto);
        
        return makeResponseEntity(traceId, HttpStatus.OK, null, "관리자 대여 물품 내역 조회 성공", result);
    }

    @PostMapping("/{rentalItem}")
    public ResponseEntity<ResponseDto<Void>> extendRentalItem(@PathVariable Long rentalItem,
                                                              @RequestBody ExtendRentalItemDto dto,
                                                              @AuthenticationPrincipal PrincipalDetails principal){
        String traceId = RequestTraceIdInterceptor.getTraceId();
        log.info("대여 연장 요청 API] TraceId={}", traceId);

        Long memberId = principal.getMember().getMemberId();

        rentalItemService.extendRentalItem(rentalItem, dto, memberId);

        return makeResponseEntity(traceId, HttpStatus.OK, null, "대여 연장 성공", null);
    }

    @PatchMapping("/{rentalItem}")
    @PreAuthorize("hasRole('ADMIN') or (hasRole('MANAGER'))")
    public ResponseEntity<ResponseDto<Void>> returnRentalItem(@PathVariable Long rentalItem,
                                                              @AuthenticationPrincipal PrincipalDetails principal){
        String traceId = RequestTraceIdInterceptor.getTraceId();
        log.info("대여 반납 요청 API] TraceId={}", traceId);

        Long memberId = principal.getMember().getMemberId();

        rentalItemService.returnRentalItem(rentalItem, memberId);

        return makeResponseEntity(traceId, HttpStatus.OK, null, "대여 반납 성공", null);
    }
}
