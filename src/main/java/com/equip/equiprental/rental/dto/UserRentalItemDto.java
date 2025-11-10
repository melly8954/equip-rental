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
public class UserRentalItemDto {
    private Long rentalItemId;
    private Long rentalId;

    private String thumbnailUrl;
    private String category;
    private String subCategory;
    private String model;
    private String serialName;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate startDate;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate endDate;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate actualReturnDate;

    private RentalItemStatus status;
    private boolean isExtended;
}
