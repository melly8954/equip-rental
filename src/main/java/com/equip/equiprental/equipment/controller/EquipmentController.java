package com.equip.equiprental.equipment.controller;

import com.equip.equiprental.auth.security.PrincipalDetails;
import com.equip.equiprental.common.controller.ResponseController;
import com.equip.equiprental.common.dto.PageResponseDto;
import com.equip.equiprental.common.dto.ResponseDto;
import com.equip.equiprental.common.dto.SearchParamDto;
import com.equip.equiprental.common.exception.CustomException;
import com.equip.equiprental.common.exception.ErrorType;
import com.equip.equiprental.common.interceptor.RequestTraceIdInterceptor;
import com.equip.equiprental.equipment.dto.*;
import com.equip.equiprental.equipment.service.EquipmentService;
import com.equip.equiprental.member.domain.MemberRole;
import com.equip.equiprental.scope.service.ManagerScopeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/equipments")
public class EquipmentController implements ResponseController {
    private final EquipmentService equipmentService;
    private final ManagerScopeService managerScopeService;

    @PostMapping("")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResponseDto<EquipmentRegisterResponse>> addEquipment(@RequestPart(value = "data") EquipmentRegisterRequest dto,
                                                                               @RequestPart(value = "files") List<MultipartFile> files) {
        String traceId = RequestTraceIdInterceptor.getTraceId();
        log.info("[장비 등록 요청 API] TraceId={}", traceId);

        EquipmentRegisterResponse result = equipmentService.register(dto, files);
        return makeResponseEntity(traceId, HttpStatus.OK, null, "신규 장비 등록 성공", result);
    }

    @GetMapping("")
    public ResponseEntity<ResponseDto<PageResponseDto<EquipmentDto>>> getEquipment(@ModelAttribute SearchParamDto paramDto) {
        String traceId = RequestTraceIdInterceptor.getTraceId();
        log.info("[장비 조회 요청 API] TraceId={}", traceId);

        PageResponseDto<EquipmentDto> result = equipmentService.getEquipment(paramDto);

        return makeResponseEntity(traceId, HttpStatus.OK, null, "장비 조회 성공", result);
    }

    @GetMapping("/{equipmentId}/items")
    @PreAuthorize("hasRole('ADMIN') or (hasRole('MANAGER'))")
    public ResponseEntity<ResponseDto<EquipmentItemListDto>> getEquipmentItem(@PathVariable Long equipmentId,
                                                                              @ModelAttribute SearchParamDto paramDto,
                                                                              @AuthenticationPrincipal PrincipalDetails principal){
        String traceId = RequestTraceIdInterceptor.getTraceId();
        log.info("[장비 아이템 조회 요청 API] TraceId={}", traceId);

        if (principal.getMember().getRole() == MemberRole.MANAGER &&
                !managerScopeService.canAccessEquipment(equipmentId, principal.getMember().getMemberId())) {
            throw new CustomException(ErrorType.FORBIDDEN);
        }

        EquipmentItemListDto result = equipmentService.getEquipmentItem(equipmentId, paramDto);

        return makeResponseEntity(traceId, HttpStatus.OK, null, "장비 아이템 조회 성공", result);
    }

    @PostMapping("/{equipmentId}/stock")
    @PreAuthorize("hasRole('ADMIN') or (hasRole('MANAGER'))")
    public ResponseEntity<ResponseDto<Void>> increaseStock(@PathVariable Long equipmentId,
                                                              @RequestBody IncreaseStockRequestDto dto,
                                                              @AuthenticationPrincipal PrincipalDetails principal){
        String traceId = RequestTraceIdInterceptor.getTraceId();
        log.info("[장비 재고 수 증가 요청 API] TraceId={}", traceId);

        if (principal.getMember().getRole() == MemberRole.MANAGER &&
                !managerScopeService.canAccessEquipment(equipmentId, principal.getMember().getMemberId())) {
            throw new CustomException(ErrorType.FORBIDDEN);
        }

        equipmentService.increaseStock(equipmentId, dto);

        return makeResponseEntity(traceId, HttpStatus.OK, null, "장비 아이템 조회 성공", null);
    }

    @PatchMapping("/{equipmentId}/status")
    @PreAuthorize("hasRole('ADMIN') or (hasRole('MANAGER'))")
    public ResponseEntity<ResponseDto<Void>> updateStatus(@PathVariable Long equipmentId,
                                                              @RequestBody UpdateItemStatusDto dto,
                                                              @AuthenticationPrincipal PrincipalDetails principal){
        String traceId = RequestTraceIdInterceptor.getTraceId();
        log.info("[장비 재고 수 증가 요청 API] TraceId={}", traceId);

        if (principal.getMember().getRole() == MemberRole.MANAGER &&
                !managerScopeService.canAccessEquipment(equipmentId, principal.getMember().getMemberId())) {
            throw new CustomException(ErrorType.FORBIDDEN);
        }

        equipmentService.increaseStock(equipmentId, dto);

        return makeResponseEntity(traceId, HttpStatus.OK, null, "장비 아이템 조회 성공", null);
    }
}
