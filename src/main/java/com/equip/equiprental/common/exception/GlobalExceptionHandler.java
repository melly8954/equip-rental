package com.equip.equiprental.common.exception;

import com.equip.equiprental.common.controller.ResponseController;
import com.equip.equiprental.common.dto.ResponseDto;
import com.equip.equiprental.common.interceptor.RequestTraceIdInterceptor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;

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

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ResponseDto<Void>> handleMaxUploadSizeExceeded(MaxUploadSizeExceededException e) {
        String traceId = RequestTraceIdInterceptor.getTraceId();

        ErrorType errorType = ErrorType.FILE_SIZE_EXCEEDED;
        log.error("TraceId: {}, 파일 첨부 업로드 크기 예외 발생 - Code: {}, Message: {}",
                traceId, errorType.getErrorCode(), errorType.getMessage());

        return makeResponseEntity(
                traceId,
                errorType.getStatus(),
                errorType.getErrorCode(),
                errorType.getMessage(),
                null
        );
    }

    @ExceptionHandler(MissingServletRequestPartException.class)
    public ResponseEntity<ResponseDto<Void>> handleMissingPart(MissingServletRequestPartException ex) {
        String traceId = RequestTraceIdInterceptor.getTraceId();

        ErrorType errorType = ErrorType.FILE_REQUIRED;
        log.error("TraceId: {}, 파일 미첨부 예외 발생 - Code: {}, Message: {}",
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
