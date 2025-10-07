package com.equip.equiprental.member.dto;

import com.equip.equiprental.member.domain.MemberRole;
import com.equip.equiprental.member.domain.MemberStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UpdateMemberRequest {
    private MemberStatus status;
    private MemberRole role;
}
