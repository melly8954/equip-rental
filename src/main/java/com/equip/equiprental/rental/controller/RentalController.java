package com.equip.equiprental.rental.controller;

import com.equip.equiprental.auth.security.PrincipalDetails;
import com.equip.equiprental.common.controller.ResponseController;
import com.equip.equiprental.common.dto.PageResponseDto;
import com.equip.equiprental.common.dto.ResponseDto;
import com.equip.equiprental.common.dto.SearchParamDto;
import com.equip.equiprental.common.exception.CustomException;
import com.equip.equiprental.common.exception.ErrorType;
import com.equip.equiprental.common.interceptor.RequestTraceIdInterceptor;
import com.equip.equiprental.member.domain.MemberRole;
import com.equip.equiprental.rental.dto.*;
import com.equip.equiprental.rental.service.iface.RentalService;
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
@RequestMapping("/api/v1/rentals")
public class RentalController implements ResponseController {

    private final RentalService rentalService;
    private final ManagerScopeService managerScopeService;

    @PostMapping("")
    public ResponseEntity<ResponseDto<RentalResponseDto>> createRental(@RequestBody RentalRequestDto dto,
                                                                       @AuthenticationPrincipal PrincipalDetails principal){
        String traceId = RequestTraceIdInterceptor.getTraceId();
        log.info("[장비 대여 신청 요청 API] TraceId={}", traceId);

        Long memberId = principal.getMember().getMemberId();

        RentalResponseDto result = rentalService.requestRental(dto, memberId);

        return makeResponseEntity(traceId, HttpStatus.OK, null, "장비 대여 신청 성공", result);
    }

    @GetMapping("")
    @PreAuthorize("hasRole('ADMIN') or (hasRole('MANAGER'))")
    public ResponseEntity<ResponseDto<PageResponseDto<AdminRentalDto>>> getAdminRentalList(@ModelAttribute SearchParamDto paramDto){
        String traceId = RequestTraceIdInterceptor.getTraceId();
        log.info("관리자 장비 대여 신청내역 조회 요청 API] TraceId={}", traceId);

        PageResponseDto<AdminRentalDto> result = rentalService.getAdminRentalList(paramDto);
        return makeResponseEntity(traceId, HttpStatus.OK, null, "관리자 장비 대여 신청내역 조히 성공", result);
    }

    @GetMapping("/me")
    public ResponseEntity<ResponseDto<PageResponseDto<UserRentalDto>>> getUserRentalList(@ModelAttribute SearchParamDto paramDto,
                                                                                         @AuthenticationPrincipal PrincipalDetails principal){
        String traceId = RequestTraceIdInterceptor.getTraceId();
        log.info("사용자 장비 대여 신청내역 조회 요청 API] TraceId={}", traceId);

        Long memberId = principal.getMember().getMemberId();

        PageResponseDto<UserRentalDto> result = rentalService.getUserRentalList(paramDto, memberId);

        return makeResponseEntity(traceId, HttpStatus.OK, null, "사용자 장비 대여 신청내역 조회 성공", result);
    }

    @PatchMapping("/{rentalId}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<ResponseDto<Void>> updateRentalStatus(@PathVariable Long rentalId,
                                                                @RequestBody UpdateRentalStatusDto dto,
                                                                @AuthenticationPrincipal PrincipalDetails principal) {
        String traceId = RequestTraceIdInterceptor.getTraceId();
        log.info("대여 신청 상태 변경 요청 API] TraceId={}", traceId);

        Long memberId = principal.getMember().getMemberId();

        if (principal.getMember().getRole() == MemberRole.MANAGER &&
                !managerScopeService.canAccessEquipment(dto.getEquipmentId(), principal.getMember().getMemberId())) {
            throw new CustomException(ErrorType.FORBIDDEN);
        }

        rentalService.updateRentalStatus(dto, rentalId, memberId);

        return makeResponseEntity(traceId, HttpStatus.OK, null, "대여 신청 상태 변경 성공", null);
    }

    @GetMapping("/{rentalId}/items")
    public ResponseEntity<ResponseDto<PageResponseDto<UserRentalItemDto>>> getUserRentalItemList(@PathVariable Long rentalId,
                                                                                                 @ModelAttribute SearchParamDto paramDto,
                                                                                                 @AuthenticationPrincipal PrincipalDetails principal) {
        String traceId = RequestTraceIdInterceptor.getTraceId();
        log.info("사용자 대여 승인 장비 리스트 조회 요청 API] TraceId={}", traceId);

        Long memberId = principal.getMember().getMemberId();

        PageResponseDto<UserRentalItemDto> result = rentalService.getUserRentalItemList(paramDto, rentalId, memberId);

        return makeResponseEntity(traceId, HttpStatus.OK, null, "사용자 대여 승인 장비 리스트 조회 성공", result);
    }
}
