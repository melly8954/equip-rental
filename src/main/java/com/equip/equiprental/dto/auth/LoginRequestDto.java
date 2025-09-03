package com.equip.equiprental.dto.auth;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@Builder
public class LoginRequestDto {
    private String username;
    private String password;
}
