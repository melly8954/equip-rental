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
    INVALID_CATEGORY_REQUEST("invalid_category_request", "잘못된 카테고리 요청입니다.", HttpStatus.BAD_REQUEST),

    // 장비 등록 에러
    INVALID_EQUIP_CATEGORY_REQUEST("invalid_equipment_category_request", "잘못된 카테고리 요청입니다.", HttpStatus.BAD_REQUEST),
    EXIST_EQUIPMENT_MODEL_CODE("exist_equipment_model_code", "해당 장비는 이미 등록된 장비입니다.", HttpStatus.BAD_REQUEST),

    // 404 에러
    EQUIPMENT_NOT_FOUND("equipment_not_found","해당 정보로 등록된 장비가 존재하지 않습니다.", HttpStatus.NOT_FOUND),
    EQUIPMENT_ITEM_NOT_FOUND("equipment_item_not_found","해당 정보로 등록된 장비 아이템이 존재하지 않습니다.", HttpStatus.NOT_FOUND),
    RENTAL_NOT_FOUND("rental_not_found","해당 정보로 등록된 장비 대여 신청내역이 존재하지 않습니다.", HttpStatus.NOT_FOUND),

    // 장비 재고 수량 처리 에러
    AMOUNT_MUST_BE_POSITIVE("amount_must_be_positive", "추가할 재고 수량은 반드시 양수여야합니다.", HttpStatus.BAD_REQUEST),

    // 장비 상태 변경
    CANNOT_MODIFY_WHILE_RENTED("cannot_modify_while_rented","대여 중인 장비는 상태 변경이 불가능합니다. 반납 처리 메뉴에서만 수정 가능합니다.", HttpStatus.CONFLICT),

    // 날짜 포맷팅 에러
    INVALID_DATE_FORMAT("invalid_date_format", "날짜 형식이 올바르지 않습니다. yyyy-MM-dd 형식으로 입력해주세요.", HttpStatus.BAD_REQUEST),

    // 장비 대여 신청 에러
    INVALID_RENTAL_START_DATE("invalid_rental_start_date", "대여 시작일은 오늘날 이후여야 합니다.", HttpStatus.BAD_REQUEST),
    INVALID_RENTAL_END_DATE("invalid_rental_end_date", "대여 종료일은 대여 시작일과 같거나 이후여야 합니다.", HttpStatus.BAD_REQUEST),
    INVALID_RENTAL_QUANTITY("invalid_rental_quantity", "대여 수량이 남은 재고보다 많습니다.", HttpStatus.BAD_REQUEST),

    // 장비 대여 승인
    EQUIPMENT_ITEM_INSUFFICIENT_STOCK("equipment_item_insufficient_stock", "해당 장비 모델의 대여 가능 재고가 부족합니다.", HttpStatus.CONFLICT),
    PARTIAL_UPDATE("partial_update", "요청한 모든 장비 아이템 상태를 업데이트하지 못했습니다.", HttpStatus.CONFLICT),

    // 파일 관련
    FILE_NOT_FOUND("file_not_found", "파일을 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    FILE_REQUIRED("file_required", "파일을 추가하지 않았습니다.", HttpStatus.BAD_REQUEST),
    FILE_UPLOAD_FAILED("file_upload_failed", "파일 업로드에 실패했습니다.", HttpStatus.INTERNAL_SERVER_ERROR),
    FILE_SIZE_EXCEEDED("file_size_exceeded", "파일 크기가 허용된 범위를 초과했습니다.", HttpStatus.PAYLOAD_TOO_LARGE);


    private final String errorCode;
    private final String message;
    private final HttpStatus status;
}
