package com.equip.equiprental.common.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorType {
    // 공통
    BAD_REQUEST("bad_request", "잘못된 요청입니다.", HttpStatus.BAD_REQUEST),
    UNAUTHORIZED("unauthorized", "인증되지 않은 사용자입니다.", HttpStatus.UNAUTHORIZED),
    FORBIDDEN("forbidden", "권한이 없습니다.", HttpStatus.FORBIDDEN),
    NOT_FOUND("not_found", "리소스를 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    CONFLICT("conflict", "요청이 현재 상태와 충돌합니다.", HttpStatus.CONFLICT),
    INTERNAL_SERVER_ERROR("internal_error", "서버 내부 오류가 발생했습니다.", HttpStatus.INTERNAL_SERVER_ERROR),

    // 쿼리 파라미터 요청 값 에러
    INVALID_ENUM_VALUE("invalid_enum_value", "잘못된 Enum 매핑 요청입니다.", HttpStatus.BAD_REQUEST),

    // 날짜 포맷팅 에러
    INVALID_DATE_FORMAT("invalid_date_format", "날짜 형식이 올바르지 않습니다. yyyy-MM-dd 형식으로 입력해주세요.", HttpStatus.BAD_REQUEST),

    // 파일 관련
    FILE_NOT_FOUND("file_not_found", "파일을 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    FILE_REQUIRED("file_required", "파일을 추가하지 않았습니다.", HttpStatus.BAD_REQUEST),
    FILE_UPLOAD_FAILED("file_upload_failed", "파일 업로드에 실패했습니다.", HttpStatus.INTERNAL_SERVER_ERROR),
    FILE_SIZE_EXCEEDED("file_size_exceeded", "파일 크기가 허용된 범위를 초과했습니다.", HttpStatus.PAYLOAD_TOO_LARGE);

    private final String errorCode;
    private final String message;
    private final HttpStatus status;
}
