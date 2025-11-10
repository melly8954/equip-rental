package com.equip.equiprental.rental.dto;

import com.equip.equiprental.rental.domain.RentalItemStatus;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@AllArgsConstructor
@Builder
public class AdminRentalItemDto {
    private Long rentalItemId;
    private Long rentalId;

    private String thumbnailUrl;
    private String category;
    private String subCategory;
    private String model;
    private String serialName;

    private String memberName;
    private String department;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate startDate;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate endDate;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate actualReturnDate;

    private RentalItemStatus status;
    private boolean isExtended;

}
