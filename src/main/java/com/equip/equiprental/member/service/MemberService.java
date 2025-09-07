package com.equip.equiprental.member.service;

import com.equip.equiprental.common.response.PageResponseDto;
import com.equip.equiprental.common.response.SearchParamDto;
import com.equip.equiprental.member.dto.*;

public interface MemberService {
    // 사용자 가입
    SignUpResponseDto signUp(SignUpRequestDto dto);
    // 사용자 목록 조회
    PageResponseDto<MemberDto> searchMembers(SearchParamDto dto);

    UpdateMemberStatusResponse updateMemberStatus(Long memberId, UpdateMemberRequest dto);
    UpdateMemberRoleResponse updateMemberRole(Long memberId, UpdateMemberRequest dto);

}
