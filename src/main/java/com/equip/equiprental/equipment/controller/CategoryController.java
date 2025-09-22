package com.equip.equiprental.equipment.controller;

import com.equip.equiprental.common.controller.ResponseController;
import com.equip.equiprental.common.dto.ResponseDto;
import com.equip.equiprental.common.interceptor.RequestTraceIdInterceptor;
import com.equip.equiprental.equipment.dto.CategoryDto;
import com.equip.equiprental.equipment.dto.SubCategoryDto;
import com.equip.equiprental.equipment.service.CategoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/categories")
public class CategoryController implements ResponseController {
    private final CategoryService categoryService;

    @GetMapping("")
    public ResponseEntity<ResponseDto<List<CategoryDto>>> getCategories() {
        String traceId = RequestTraceIdInterceptor.getTraceId();
        log.info("[모든 카테고리 조회 요청 API] TraceId={}", traceId);

        List<CategoryDto> result = categoryService.getCategories();
        return makeResponseEntity(traceId, HttpStatus.OK, null, "모든 카테고리 조회 성공", result);
    }

    @GetMapping("/{categoryId}/sub-categories")
    public ResponseEntity<ResponseDto<List<SubCategoryDto>>> getSubCategories(@PathVariable Long categoryId) {
        String traceId = RequestTraceIdInterceptor.getTraceId();
        log.info("[서브 카테고리 조회 요청 API] TraceId={}", traceId);

        List<SubCategoryDto> result = categoryService.getSubCategories(categoryId);
        return makeResponseEntity(traceId, HttpStatus.OK, null, "서브 카테고리 조회 성공", result);
    }
}
