package com.equip.equiprental.member.service;

import com.equip.equiprental.common.exception.CustomException;
import com.equip.equiprental.common.exception.ErrorType;
import com.equip.equiprental.common.dto.PageResponseDto;
import com.equip.equiprental.common.dto.SearchParamDto;
import com.equip.equiprental.equipment.domain.Category;
import com.equip.equiprental.member.domain.Department;
import com.equip.equiprental.member.domain.Member;
import com.equip.equiprental.member.dto.*;
import com.equip.equiprental.member.repository.DepartmentRepository;
import com.equip.equiprental.member.repository.MemberRepository;
import com.equip.equiprental.member.domain.MemberRole;
import com.equip.equiprental.member.domain.MemberStatus;
import com.equip.equiprental.member.service.iface.MemberService;
import com.equip.equiprental.scope.repository.ManagerScopeRepository;
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
    private final DepartmentRepository departmentRepository;
    private final PasswordEncoder passwordEncoder;
    private final ManagerScopeRepository managerScopeRepository;

    @Override
    @Transactional
    public SignUpResponse signUp(SignUpRequest dto) {
        if(memberRepository.existsByUsername(dto.getUsername())){
            throw new CustomException(ErrorType.DUPLICATE_USERNAME);
        }

        if(memberRepository.existsByEmail(dto.getEmail())){
            throw new CustomException(ErrorType.DUPLICATE_EMAIL);
        }

        if(!dto.getPassword().equals(dto.getConfirmPassword())){
            throw new CustomException(ErrorType.PASSWORD_MISMATCH);
        }

        Department department = departmentRepository.findById(dto.getDepartmentId())
                .orElseThrow(() -> new CustomException(ErrorType.NOT_FOUND));

        Member member = Member.builder()
                .username(dto.getUsername())
                .password(passwordEncoder.encode(dto.getPassword()))
                .name(dto.getName())
                .department(department)
                .email(dto.getEmail())
                .role(MemberRole.USER)
                .status(MemberStatus.PENDING)
                .build();
        memberRepository.save(member);

        return SignUpResponse.builder()
                .memberId(member.getMemberId())
                .username(member.getUsername())
                .name(member.getName())
                .department(department.getDepartmentName())
                .email(member.getEmail())
                .createdAt(member.getCreatedAt())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponseDto<MemberDto> searchMembers(SearchParamDto dto) {
        Pageable pageable = dto.getPageable();

        MemberStatus status = dto.getMemberStatusEnum();
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
                .map(member -> {
                    List<String> categories = null;
                    if(member.getRole() == MemberRole.MANAGER) {
                        // 매니저의 카테고리 스코프 찾기
                        categories = managerScopeRepository.findCategoriesByManager(member.getMemberId())
                                .stream()
                                .map(Category::getCategoryId) // 또는 getLabel
                                .map(String::valueOf)
                                .toList();
                    }
                    return new MemberDto(member, categories);
                })
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

    @Override
    @Transactional
    public UpdateMemberStatusResponse updateMemberStatus(Long memberId, UpdateMemberRequest dto) {
        Member member = memberRepository.findByMemberId(memberId)
                .orElseThrow(() -> new CustomException(ErrorType.USER_NOT_FOUND));

        // 상태 변경 전
        String oldStatus = member.getStatus().name();

        member.updateStatus(dto.getStatusEnum());

        return UpdateMemberStatusResponse.builder()
                .memberId(member.getMemberId())
                .oldStatus(oldStatus)
                .newStatus(member.getStatus().name())
                .build();
    }

    @Override
    @Transactional
    public UpdateMemberRoleResponse updateMemberRole(Long memberId, UpdateMemberRequest dto) {
        Member member = memberRepository.findByMemberId(memberId)
                .orElseThrow(() -> new CustomException(ErrorType.USER_NOT_FOUND));

        // 상태 변경 전
        String oldRole = member.getRole().name();

        member.updateRole(dto.getRoleEnum());

        return UpdateMemberRoleResponse.builder()
                .memberId(member.getMemberId())
                .oldRole(oldRole)
                .newRole(member.getRole().name())
                .build();
    }
}
