package com.equip.equiprental.common.dto;

import com.equip.equiprental.common.exception.CustomException;
import com.equip.equiprental.common.exception.ErrorType;
import com.equip.equiprental.equipment.domain.EquipmentStatus;
import com.equip.equiprental.member.domain.MemberRole;
import com.equip.equiprental.member.domain.MemberStatus;
import com.equip.equiprental.rental.domain.RentalStatus;
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
    @Builder.Default
    private int page = 1;
    @Builder.Default
    private int size = 10;
    private String memberStatus;
    private String role;

    private String category;
    private String subCategory;
    private String model;

    private String equipmentStatus;
    private String rentalStatus;

    private String memberName;
    private String department;

    public Pageable getPageable() {
        return PageRequest.of(page - 1, size, Sort.by("createdAt").descending());
    }

    public MemberStatus getMemberStatusEnum() {
        if (memberStatus == null || memberStatus.isBlank()) return null;
        try {
            return MemberStatus.valueOf(memberStatus.toUpperCase());
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

    public EquipmentCategory getCategoryEnum() {
        if (category == null || category.isBlank()) return null;
        try{
            return EquipmentCategory.valueOf(category.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new CustomException(ErrorType.INVALID_CATEGORY_REQUEST);
        }
    }

    public EquipmentStatus getEquipmentStatusEnum() {
        if (equipmentStatus == null || equipmentStatus.isBlank()) return null;
        try {
            return EquipmentStatus.valueOf(equipmentStatus.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new CustomException(ErrorType.INVALID_STATUS_REQUEST);
        }
    }

    public RentalStatus getRentalStatusEnum() {
        if (rentalStatus == null || rentalStatus.isBlank()) return null;
        try {
            return RentalStatus.valueOf(rentalStatus.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new CustomException(ErrorType.INVALID_STATUS_REQUEST);
        }
    }
}