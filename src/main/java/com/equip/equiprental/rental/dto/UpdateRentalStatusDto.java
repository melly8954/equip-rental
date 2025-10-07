package com.equip.equiprental.rental.dto;

import com.equip.equiprental.rental.domain.RentalStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class UpdateRentalStatusDto {
    private Long equipmentId;
    private RentalStatus newStatus;
    private String rejectReason;
}
