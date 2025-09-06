package com.equip.equiprental.member.dto;

import lombok.*;

@Getter
@AllArgsConstructor
@Builder
public class MemberStatusDto {
    private Long memberId;
    private String oldStatus;
    private String newStatus;
}
