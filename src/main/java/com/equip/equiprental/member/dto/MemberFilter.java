package com.equip.equiprental.member.dto;

import com.equip.equiprental.common.dto.SearchParamDto;
import com.equip.equiprental.member.domain.MemberRole;
import com.equip.equiprental.member.domain.MemberStatus;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class MemberFilter extends SearchParamDto {
    private MemberStatus status;
    private MemberRole role;
}
