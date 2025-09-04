package com.equip.equiprental.member.service;

import com.equip.equiprental.common.exception.CustomException;
import com.equip.equiprental.common.exception.ErrorType;
import com.equip.equiprental.member.domain.Member;
import com.equip.equiprental.member.repository.MemberRepository;
import com.equip.equiprental.member.domain.MemberRole;
import com.equip.equiprental.member.domain.MemberStatus;
import com.equip.equiprental.member.dto.SignUpRequestDto;
import com.equip.equiprental.member.dto.SignUpResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MemberServiceImpl implements MemberService {
    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public SignUpResponseDto signUp(SignUpRequestDto dto) {
        if(memberRepository.existsByUsername(dto.getUsername())){
            throw new CustomException(ErrorType.DUPLICATE_USERNAME);
        }

        if(memberRepository.existsByEmail(dto.getEmail())){
            throw new CustomException(ErrorType.DUPLICATE_EMAIL);
        }

        if(!dto.getPassword().equals(dto.getConfirmPassword())){
            throw new CustomException(ErrorType.PASSWORD_MISMATCH);
        }

        Member member = Member.builder()
                .username(dto.getUsername())
                .password(passwordEncoder.encode(dto.getPassword()))
                .name(dto.getName())
                .department(dto.getDepartment())
                .email(dto.getEmail())
                .role(MemberRole.USER)
                .status(MemberStatus.PENDING)
                .build();
        memberRepository.save(member);

        return SignUpResponseDto.builder()
                .memberId(member.getMemberId())
                .username(member.getUsername())
                .name(member.getName())
                .department(member.getDepartment())
                .email(member.getEmail())
                .createdAt(member.getCreatedAt())
                .build();
    }
}
