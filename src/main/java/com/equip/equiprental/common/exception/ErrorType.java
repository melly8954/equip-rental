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

    // 회원가입
    DUPLICATE_USERNAME("duplicate_username", "이미 사용 중인 아이디입니다.", HttpStatus.CONFLICT),
    DUPLICATE_EMAIL("duplicate_email", "이미 사용 중인 이메일입니다.", HttpStatus.CONFLICT),
    PASSWORD_MISMATCH("password_mismatch", "비밀번호와 비밀번호 확인이 일치하지 않습니다.", HttpStatus.BAD_REQUEST),

    BAD_CREDENTIALS("bad_credentials", "비밀번호가 일치하지 않습니다.", HttpStatus.UNAUTHORIZED),
    USER_NOT_FOUND("user_not_found", "해당 정보로 등록된 사용자가 없습니다.", HttpStatus.NOT_FOUND),
    USER_PENDING("user_pending", "미승인 계정입니다.", HttpStatus.UNAUTHORIZED),
    USER_DELETED("user_deleted", "탈퇴된 계정입니다.", HttpStatus.UNAUTHORIZED),

    // 쿼리 파라미터 요청 값 에러
    INVALID_STATUS_REQUEST("invalid_status_request", "잘못된 상태 요청입니다.", HttpStatus.BAD_REQUEST),
    INVALID_ROLE_REQUEST("invalid_role_request", "잘못된 역할 요청입니다.", HttpStatus.BAD_REQUEST),

    // 장비 등록 에러
    INVALID_EQUIP_CATEGORY_REQUEST("invalid_equipment_category_request", "잘못된 카테고리 요청입니다.", HttpStatus.BAD_REQUEST),
    EXIST_EQUIPMENT_MODEL_CODE("exist_equipment_model_code", "해당 장비는 이미 등록된 장비입니다.", HttpStatus.BAD_REQUEST),

    // 파일 관련
    FILE_NOT_FOUND("file_not_found", "파일을 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    FILE_UPLOAD_FAILED("file_upload_failed", "파일 업로드에 실패했습니다.", HttpStatus.INTERNAL_SERVER_ERROR),
    FILE_SIZE_EXCEEDED("file_size_exceeded", "파일 크기가 허용된 범위를 초과했습니다.", HttpStatus.PAYLOAD_TOO_LARGE);


    private final String errorCode;
    private final String message;
    private final HttpStatus status;
}
