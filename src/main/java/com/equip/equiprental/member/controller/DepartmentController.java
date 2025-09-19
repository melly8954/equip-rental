package com.equip.equiprental.member.controller;

import com.equip.equiprental.common.controller.ResponseController;
import com.equip.equiprental.common.dto.ResponseDto;
import com.equip.equiprental.common.interceptor.RequestTraceIdInterceptor;
import com.equip.equiprental.member.dto.DepartmentDto;
import com.equip.equiprental.member.service.DepartmentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/departments")
public class DepartmentController implements ResponseController {
    private final DepartmentService departmentService;

    @GetMapping("")
    public ResponseEntity<ResponseDto<List<DepartmentDto>>> getAll() {
        String traceId = RequestTraceIdInterceptor.getTraceId();
        log.info("모든 부서명 조회 요청 API] TraceId={}", traceId);

        List<DepartmentDto> result = departmentService.getDepartmentList();
        return makeResponseEntity(traceId, HttpStatus.OK, null, "모든 부서명 조회 성공", result);
    }
}
