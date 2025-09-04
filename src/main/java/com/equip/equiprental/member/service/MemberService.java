package com.equip.equiprental.member.service;

import com.equip.equiprental.member.dto.SignUpRequestDto;
import com.equip.equiprental.member.dto.SignUpResponseDto;

public interface MemberService {
    SignUpResponseDto signUp(SignUpRequestDto dto);
}
