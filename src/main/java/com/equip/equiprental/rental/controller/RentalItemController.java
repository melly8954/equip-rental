package com.equip.equiprental.rental.controller;

import com.equip.equiprental.auth.security.PrincipalDetails;
import com.equip.equiprental.common.controller.ResponseController;
import com.equip.equiprental.common.dto.PageResponseDto;
import com.equip.equiprental.common.dto.ResponseDto;
import com.equip.equiprental.common.dto.SearchParamDto;
import com.equip.equiprental.common.interceptor.RequestTraceIdInterceptor;
import com.equip.equiprental.rental.dto.AdminRentalItemDto;
import com.equip.equiprental.rental.dto.UserRentalItemDto;
import com.equip.equiprental.rental.service.RentalItemService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/rental-items")
public class RentalItemController implements ResponseController {

    private final RentalItemService rentalItemService;

    @GetMapping("")
    @PreAuthorize("hasRole('ADMIN') or (hasRole('MANAGER'))")
    public ResponseEntity<ResponseDto<PageResponseDto<AdminRentalItemDto>>> getAdminRentalItemList(@ModelAttribute SearchParamDto paramDto){
        String traceId = RequestTraceIdInterceptor.getTraceId();
        log.info("관리자 장비 대여 물품내역 조회 요청 API] TraceId={}", traceId);

        PageResponseDto<AdminRentalItemDto> result = rentalItemService.getAdminRentalItemLists(paramDto);
        
        return makeResponseEntity(traceId, HttpStatus.OK, null, "관리자 장비 대여 물품내역 조회 성공", result);
    }

    @GetMapping("/me")
    public ResponseEntity<ResponseDto<PageResponseDto<UserRentalItemDto>>> getUserRentalItemList(@ModelAttribute SearchParamDto paramDto,
                                                                                                 @AuthenticationPrincipal PrincipalDetails principal){
        String traceId = RequestTraceIdInterceptor.getTraceId();
        log.info("사용자 장비 대여 물품내역 조회 요청 API] TraceId={}", traceId);

        Long memberId = principal.getMember().getMemberId();

        PageResponseDto<UserRentalItemDto> result = rentalItemService.getUserRentalItemLists(paramDto, memberId);

        return makeResponseEntity(traceId, HttpStatus.OK, null, "사용자 장비 대여 물품내역 조회 성공", result);
    }
}
