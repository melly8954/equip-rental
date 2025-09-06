package com.equip.equiprental.member.dto;

import com.equip.equiprental.common.exception.CustomException;
import com.equip.equiprental.common.exception.ErrorType;
import com.equip.equiprental.member.domain.MemberRole;
import com.equip.equiprental.member.domain.MemberStatus;
import lombok.Getter;

@Getter
public class UpdateMemberRequestDto {
    private String updateStatus;
    private String updateRole;

    public MemberStatus getStatusEnum() {
        if (updateStatus == null) throw new CustomException(ErrorType.INVALID_STATUS_REQUEST);
        try {
            return MemberStatus.valueOf(updateStatus.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new CustomException(ErrorType.INVALID_STATUS_REQUEST);
        }
    }

    public MemberRole getRoleEnum() {
        if (updateRole == null) throw new CustomException(ErrorType.INVALID_ROLE_REQUEST);
        try {
            return MemberRole.valueOf(updateRole.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new CustomException(ErrorType.INVALID_ROLE_REQUEST);
        }
    }
}
