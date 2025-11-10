package com.equip.equiprental.auth.service.iface;

import com.equip.equiprental.auth.dto.LoginRequestDto;
import com.equip.equiprental.auth.dto.LoginResponseDto;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public interface AuthService {
    LoginResponseDto login(HttpServletRequest httpRequest, LoginRequestDto dto);
    void logout(HttpServletRequest httpRequest, HttpServletResponse httpResponse);
}
