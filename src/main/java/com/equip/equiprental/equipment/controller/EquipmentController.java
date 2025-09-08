package com.equip.equiprental.equipment.controller;

import com.equip.equiprental.common.controller.ResponseController;
import com.equip.equiprental.common.dto.ResponseDto;
import com.equip.equiprental.common.interceptor.RequestTraceIdInterceptor;
import com.equip.equiprental.equipment.dto.EquipmentRegisterRequest;
import com.equip.equiprental.equipment.dto.EquipmentRegisterResponse;
import com.equip.equiprental.equipment.service.EquipmentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/equipments")
public class EquipmentController implements ResponseController {
    private final EquipmentService equipmentService;

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("")
    public ResponseEntity<ResponseDto<EquipmentRegisterResponse>> addEquipment(@RequestPart(value = "data") EquipmentRegisterRequest dto,
                                                                               @RequestPart(value = "files") List<MultipartFile> files) {
        String traceId = RequestTraceIdInterceptor.getTraceId();
        log.info("[장비 등록 요청 API] TraceId={}", traceId);

        EquipmentRegisterResponse result = equipmentService.register(dto, files);
        return makeResponseEntity(traceId, HttpStatus.OK, null, "신규 장비 등록 성공", result);
    }

}
