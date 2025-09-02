package com.equip.equiprental.dto.member;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class SignUpRequestDto {
    private String username;
    private String password;
    private String confirmPassword;
    private String name;
    private String department;
    private String email;
}
