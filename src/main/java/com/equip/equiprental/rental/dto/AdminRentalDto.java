package com.equip.equiprental.rental.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
@Builder
public class AdminRentalDto {
    private Long rentalId;
    private Long equipmentId;
    private String thumbnailUrl;
    private int quantity;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate requestStartDate;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate requestEndDate;
    private String rentalReason;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDateTime createdAt;

    private Long memberId;
    private String name;
    private String department;

    private String category;
    private String subCategory;
    private String model;
}
