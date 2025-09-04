package com.equip.equiprental.common.response;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ResponseDto<T> {
    private String id;
    private int code;
    private String errorCode;
    private String message;
    private T data;
}

