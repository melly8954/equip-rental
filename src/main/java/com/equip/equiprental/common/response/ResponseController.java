package com.equip.equiprental.common.response;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public interface ResponseController {
    default <T> ResponseEntity<ResponseDto<T>> makeResponseEntity(String traceId, HttpStatus httpStatus, String errorCode, String message, T data) {
        ResponseDto<T> responseDto = ResponseDto.<T>builder()
                .id(traceId)
                .code(httpStatus.value())
                .errorCode(errorCode)
                .message(message)
                .data(data)
                .build();
        return ResponseEntity.status(httpStatus).body(responseDto);
    }
}