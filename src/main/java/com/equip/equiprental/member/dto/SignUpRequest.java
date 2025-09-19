package com.equip.equiprental.member.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@Builder
public class SignUpRequest {
    private String username;
    private String password;
    private String confirmPassword;
    private String name;
    private Long departmentId;
    private String email;
}
