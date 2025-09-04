package com.equip.equiprental.common.exception;

import com.equip.equiprental.common.response.ResponseController;
import com.equip.equiprental.common.response.ResponseDto;
import com.equip.equiprental.common.interceptor.RequestTraceIdInterceptor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler implements ResponseController {
    @ExceptionHandler(CustomException.class)
    public ResponseEntity<ResponseDto<Void>> handleCustomException(CustomException e) {
        String traceId = RequestTraceIdInterceptor.getTraceId();
        ErrorType errorType = e.getErrorType();
        log.error("TraceId: {}, 비즈니스 로직 예외 발생 - Code: {}, Message: {}",
                traceId, errorType.getErrorCode(), errorType.getMessage());

        return makeResponseEntity(
                traceId,
                errorType.getStatus(),
                errorType.getErrorCode(),
                errorType.getMessage(),
                null
        );
    }
}
