package com.equip.equiprental.member.service;

import com.equip.equiprental.common.exception.CustomException;
import com.equip.equiprental.common.exception.ErrorType;
import com.equip.equiprental.common.response.PageResponseDto;
import com.equip.equiprental.common.response.SearchParamDto;
import com.equip.equiprental.member.domain.Member;
import com.equip.equiprental.member.dto.MemberDto;
import com.equip.equiprental.member.repository.MemberRepository;
import com.equip.equiprental.member.domain.MemberRole;
import com.equip.equiprental.member.domain.MemberStatus;
import com.equip.equiprental.member.dto.SignUpRequestDto;
import com.equip.equiprental.member.dto.SignUpResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

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

    @Override
    public PageResponseDto<MemberDto> searchMembers(SearchParamDto dto) {
        Pageable pageable = dto.getPageable();

        MemberStatus status = dto.getStatusEnum();
        MemberRole role = dto.getRoleEnum();

        Page<Member> page;

        if(status != null && role != null) {
            page = memberRepository.findByStatusAndRole(status, role, pageable);
        } else if(status != null) {
            page = memberRepository.findByStatus(status, pageable);
        } else if(role != null) {
            page = memberRepository.findByRole(role, pageable);
        } else {
            page = memberRepository.findAll(pageable);
        }

        List<MemberDto> content = page.getContent()
                .stream()
                .map(MemberDto::new)
                .toList();

        return PageResponseDto.<MemberDto>builder()
                .content(content)
                .page(page.getNumber() + 1)                // 0-based → 1-based
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .numberOfElements(page.getNumberOfElements())
                .first(page.isFirst())
                .last(page.isLast())
                .empty(page.isEmpty())
                .build();
    }
}
