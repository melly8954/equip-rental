package com.equip.equiprental.rental.controller;

import com.equip.equiprental.auth.security.PrincipalDetails;
import com.equip.equiprental.common.controller.ResponseController;
import com.equip.equiprental.common.dto.ResponseDto;
import com.equip.equiprental.common.interceptor.RequestTraceIdInterceptor;
import com.equip.equiprental.rental.dto.RentalRequestDto;
import com.equip.equiprental.rental.dto.RentalResponseDto;
import com.equip.equiprental.rental.service.RentalService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/rentals")
public class RentalController implements ResponseController {

    private final RentalService rentalService;

    @PostMapping("")
    public ResponseEntity<ResponseDto<RentalResponseDto>> createRental(@RequestBody RentalRequestDto dto,
                                                                       @AuthenticationPrincipal PrincipalDetails principal){
        String traceId = RequestTraceIdInterceptor.getTraceId();
        log.info("[장비 대여 신청 요청 API] TraceId={}", traceId);

        Long memberId = principal.getMember().getMemberId();

        RentalResponseDto result = rentalService.requestRental(dto, memberId);

        return makeResponseEntity(traceId, HttpStatus.OK, null, "장비 대여 신청 성공", result);
    }
}
