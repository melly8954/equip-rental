package com.equip.equiprental.member.service.iface;

import com.equip.equiprental.common.dto.PageResponseDto;
import com.equip.equiprental.common.dto.SearchParamDto;
import com.equip.equiprental.member.dto.*;

public interface MemberService {
    // 사용자 가입
    SignUpResponse signUp(SignUpRequest dto);
    // 사용자 목록 조회
    PageResponseDto<MemberDto> searchMembers(SearchParamDto dto);

    UpdateMemberStatusResponse updateMemberStatus(Long memberId, UpdateMemberRequest dto);
    UpdateMemberRoleResponse updateMemberRole(Long memberId, UpdateMemberRequest dto);

}
