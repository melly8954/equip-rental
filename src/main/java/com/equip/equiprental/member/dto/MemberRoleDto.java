package com.equip.equiprental.member.dto;

import lombok.*;

@Getter
@AllArgsConstructor
@Builder
public class MemberRoleDto {
    private Long memberId;
    private String oldRole;
    private String newRole;
}
