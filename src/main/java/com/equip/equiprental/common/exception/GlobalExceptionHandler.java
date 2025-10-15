package com.equip.equiprental.common.exception;

import com.equip.equiprental.common.controller.ResponseController;
import com.equip.equiprental.common.dto.ResponseDto;
import com.equip.equiprental.common.interceptor.RequestTraceIdInterceptor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;

import java.util.Arrays;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler implements ResponseController {
    @ExceptionHandler(CustomException.class)
    public ResponseEntity<ResponseDto<Void>> handleCustomException(CustomException e) {
        String traceId = RequestTraceIdInterceptor.getTraceId();
        ErrorType errorType = e.getErrorType();
        String message = e.getMessage();
        log.error("TraceId: {}, 비즈니스 로직 예외 발생 - Code: {}, Message: {}",
                traceId, errorType.getErrorCode(), message);

        return makeResponseEntity(
                traceId,
                errorType.getStatus(),
                errorType.getErrorCode(),
                message,
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

    // 클라이언트 요청 본문(JSON) 역직렬화 중 발생하는 예외 처리
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ResponseDto<Void>> handleRequestBodyDeserializationError(HttpMessageNotReadableException ex) {
        String traceId = RequestTraceIdInterceptor.getTraceId();

        Throwable cause = ex.getCause();

        // 날짜 파싱 실패
        if (cause instanceof java.time.format.DateTimeParseException) {
            ErrorType errorType = ErrorType.INVALID_DATE_FORMAT;
            return makeResponseEntity(traceId, errorType.getStatus(), errorType.getErrorCode(), errorType.getMessage(), null);
        }

        // Enum 변환 실패 (JsonMappingException 내부 cause 확인)
        if (cause instanceof com.fasterxml.jackson.databind.exc.InvalidFormatException invalidFormatException) {
            Class<?> targetType = invalidFormatException.getTargetType();

            if (targetType.isEnum()) {
                ErrorType errorType = ErrorType.INVALID_ENUM_VALUE;
                Object invalidValue = invalidFormatException.getValue();
                return makeResponseEntity(
                        traceId,
                        errorType.getStatus(),
                        errorType.getErrorCode(),
                        String.format("'%s'는 잘못된 Enum 값입니다. (허용된 값: %s)",
                                invalidValue,
                                Arrays.toString(targetType.getEnumConstants())),
                        null
                );
            }
        }

        // 그 외 일반 메시지 바인딩 실패
        ErrorType errorType = ErrorType.BAD_REQUEST;
        return makeResponseEntity(traceId, errorType.getStatus(), errorType.getErrorCode(), ex.getMessage(), null);
    }

    // 요청 파라미터(@RequestParam, @PathVariable, @ModelAttribute) 변환 실패 처리
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ResponseDto<Void>> handleRequestParameterBindingError(MethodArgumentNotValidException ex) {
        String traceId = RequestTraceIdInterceptor.getTraceId();

        for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
            if ("typeMismatch".equals(fieldError.getCode())) {
                // Enum 변환 실패의 가능성이 높은 경우
                ErrorType errorType = ErrorType.INVALID_ENUM_VALUE;
                return makeResponseEntity(
                        traceId,
                        errorType.getStatus(),
                        errorType.getErrorCode(),
                        String.format("'%s' 필드에 잘못된 Enum 값: %s", fieldError.getField(), fieldError.getRejectedValue()),
                        null
                );
            }
        }

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
