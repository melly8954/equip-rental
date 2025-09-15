package com.equip.equiprental.rental.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@AllArgsConstructor
@Builder
public class UserRentalDto {
    private Long rentalId;
    private String model;
    private String category;
    private String subCategory;
    private String thumbnailUrl;
    private LocalDate requestStartDate;
    private LocalDate requestEndDate;
    private Integer quantity;
    private String status;       // WAITING, APPROVED, REJECTED
    private String rejectReason; // 상태가 REJECTED일 때만
}
