package com.equip.equiprental.equipment.controller;

import com.equip.equiprental.auth.security.PrincipalDetails;
import com.equip.equiprental.common.controller.ResponseController;
import com.equip.equiprental.common.dto.PageResponseDto;
import com.equip.equiprental.common.dto.ResponseDto;
import com.equip.equiprental.common.dto.SearchParamDto;
import com.equip.equiprental.common.exception.CustomException;
import com.equip.equiprental.common.exception.ErrorType;
import com.equip.equiprental.common.interceptor.RequestTraceIdInterceptor;
import com.equip.equiprental.equipment.dto.EquipmentItemHistoryDto;
import com.equip.equiprental.equipment.dto.UpdateItemStatusDto;
import com.equip.equiprental.equipment.service.iface.EquipmentItemService;
import com.equip.equiprental.member.domain.MemberRole;
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
@RequestMapping("/api/v1/equipment-items")
public class EquipmentItemController implements ResponseController {
    private final EquipmentItemService equipmentItemService;
    private final ManagerScopeService managerScopeService;

    @PatchMapping("/status")
    @PreAuthorize("hasRole('ADMIN') or (hasRole('MANAGER'))")
    public ResponseEntity<ResponseDto<Void>> updateStatus(@RequestBody UpdateItemStatusDto dto,
                                                          @AuthenticationPrincipal PrincipalDetails principal){
        String traceId = RequestTraceIdInterceptor.getTraceId();
        log.info("[장비 아이템 상태 변경 요청 API] TraceId={}", traceId);

        if (principal.getMember().getRole() == MemberRole.MANAGER &&
                !managerScopeService.canAccessEquipmentByItem(dto.getEquipmentItemId(), principal.getMember().getMemberId())) {
            throw new CustomException(ErrorType.FORBIDDEN);
        }

        equipmentItemService.updateItemStatus(dto, principal.getMember());

        return makeResponseEntity(traceId, HttpStatus.OK, null, "장비 아이템 상태 변경 성공", null);
    }

    @GetMapping("/{equipmentItemId}/history")
    @PreAuthorize("hasRole('ADMIN') or (hasRole('MANAGER'))")
    public ResponseEntity<ResponseDto<PageResponseDto<EquipmentItemHistoryDto>>> getItemHistory(@PathVariable Long equipmentItemId,
                                                                                                @ModelAttribute SearchParamDto paramDto,
                                                                                                @AuthenticationPrincipal PrincipalDetails principal) {
        String traceId = RequestTraceIdInterceptor.getTraceId();
        log.info("[장비 아이템 히스토리 조회 요청 API] TraceId={}", traceId);

        if (principal.getMember().getRole() == MemberRole.MANAGER &&
                !managerScopeService.canAccessEquipmentByItem(equipmentItemId, principal.getMember().getMemberId())) {
            throw new CustomException(ErrorType.FORBIDDEN);
        }

        PageResponseDto<EquipmentItemHistoryDto> result = equipmentItemService.getItemHistory(equipmentItemId, paramDto);

        return makeResponseEntity(traceId, HttpStatus.OK, null, "장비 아이템 히스토리 조회 성공", result);
    }
}
