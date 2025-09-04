package com.equip.equiprental.common.response;

import com.equip.equiprental.common.exception.CustomException;
import com.equip.equiprental.common.exception.ErrorType;
import com.equip.equiprental.member.domain.MemberRole;
import com.equip.equiprental.member.domain.MemberStatus;
import lombok.*;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SearchParamDto {
    private int page = 1;
    private int size = 10;
    private String status;
    private String role;

    public Pageable getPageable() {
        return PageRequest.of(page - 1, size, Sort.by("createdAt").descending());
    }

    public MemberStatus getStatusEnum() {
        if (status == null || status.isBlank()) return null;
        try {
            return MemberStatus.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new CustomException(ErrorType.INVALID_STATUS_REQUEST);
        }
    }

    public MemberRole getRoleEnum() {
        if (role == null || role.isBlank()) return null;
        try {
            return MemberRole.valueOf(role.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new CustomException(ErrorType.INVALID_ROLE_REQUEST);
        }
    }
}