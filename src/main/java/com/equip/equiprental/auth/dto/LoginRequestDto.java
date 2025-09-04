package com.equip.equiprental.auth.dto;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@Builder
public class LoginRequestDto {
    private String username;
    private String password;
}
