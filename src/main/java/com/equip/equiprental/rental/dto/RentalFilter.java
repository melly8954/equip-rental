package com.equip.equiprental.rental.dto;

import com.equip.equiprental.common.dto.SearchParamDto;
import com.equip.equiprental.rental.domain.RentalItemStatus;
import com.equip.equiprental.rental.domain.RentalStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class RentalFilter extends SearchParamDto {
    private Long categoryId;
    private Long subCategoryId;
    private String memberName;
    private Long departmentId;
    private RentalStatus status;
    private RentalItemStatus itemStatus;
}
