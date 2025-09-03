package com.equip.equiprental.service;

import com.equip.equiprental.dto.auth.LoginRequestDto;
import com.equip.equiprental.dto.auth.LoginResponseDto;
import jakarta.servlet.http.HttpServletRequest;

public interface AuthService {
    LoginResponseDto login(HttpServletRequest httpRequest, LoginRequestDto dto);
}
