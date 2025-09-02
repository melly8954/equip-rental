package com.equip.equiprental.service;

import com.equip.equiprental.dto.member.SignUpRequestDto;
import com.equip.equiprental.dto.member.SignUpResponseDto;

public interface MemberService {
    SignUpResponseDto signUp(SignUpRequestDto dto);
}
