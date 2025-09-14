package com.equip.equiprental.common.exception;

import com.equip.equiprental.common.controller.ResponseController;
import com.equip.equiprental.common.dto.ResponseDto;
import com.equip.equiprental.common.interceptor.RequestTraceIdInterceptor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
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

    // JSON 파싱(역직렬화, 클라이언트가 보낸 JSON → DTO로 바인딩하는 과정) 실패 시
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ResponseDto<Void>> handleInvalidDateFormat(HttpMessageNotReadableException ex) {
        String traceId = RequestTraceIdInterceptor.getTraceId();

        Throwable cause = ex.getCause();
        if (cause instanceof java.time.format.DateTimeParseException) {
            log.error("TraceId: {}, 날짜 파싱 실패: {}", traceId, cause.getMessage());

            // 여기서 ErrorType 사용
            ErrorType errorType = ErrorType.INVALID_DATE_FORMAT;

            return makeResponseEntity(
                    traceId,
                    errorType.getStatus(),
                    errorType.getErrorCode(),
                    errorType.getMessage(),
                    null
            );
        }

        // 기타 HttpMessageNotReadableException 처리
        log.error("TraceId: {}, 메시지 바인딩 실패: {}", traceId, ex.getMessage());
        ErrorType errorType = ErrorType.BAD_REQUEST;
        return makeResponseEntity(
                traceId,
                errorType.getStatus(),
                errorType.getErrorCode(),
                errorType.getMessage(),
                null
        );
    }

}
