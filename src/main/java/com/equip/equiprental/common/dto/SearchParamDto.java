package com.equip.equiprental.common.dto;

import com.equip.equiprental.board.domain.BoardType;
import com.equip.equiprental.common.exception.CustomException;
import com.equip.equiprental.common.exception.ErrorType;
import com.equip.equiprental.equipment.domain.EquipmentStatus;
import com.equip.equiprental.notification.domain.NotificationStatus;
import com.equip.equiprental.rental.domain.RentalItemStatus;
import com.equip.equiprental.rental.domain.RentalStatus;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class SearchParamDto {
    @Builder.Default
    private int page = 1;
    @Builder.Default
    private int size = 10;

    private Long categoryId;
    private Long subCategoryId;
    private String model;

    private String rentalStatus;
    private RentalItemStatus rentalItemStatus;

    private String memberName;
    private Long departmentId;

    private BoardType boardType;
    private String searchType;
    private String keyword;

    private Long boardId;

    private NotificationStatus notificationStatus;

    public Pageable getPageable() {
        return PageRequest.of(page - 1, size, Sort.by("createdAt").descending());
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