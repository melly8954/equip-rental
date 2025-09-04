package com.equip.equiprental.service;

import com.equip.equiprental.dto.auth.LoginRequestDto;
import com.equip.equiprental.dto.auth.LoginResponseDto;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public interface AuthService {
    LoginResponseDto login(HttpServletRequest httpRequest, LoginRequestDto dto);
    void logout(HttpServletRequest httpRequest, HttpServletResponse httpResponse);
}
