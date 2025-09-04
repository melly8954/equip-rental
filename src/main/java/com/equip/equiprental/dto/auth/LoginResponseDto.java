package com.equip.equiprental.dto.auth;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@Builder
public class LoginResponseDto {
    private Long memberId;
    private String username;
    private String name;
    private String department;
    private String email;
    private String role;
    private String status;
}
