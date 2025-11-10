package com.equip.equiprental.rental.dto;

import com.equip.equiprental.rental.domain.RentalStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class RentalResponseDto {
    private Long rentalId;                       // 대여 신청 ID
    private Long equipmentId;                    // 기자재 모델 ID
    private Integer quantity;
    private RentalStatus status;                 // 대여 신청 상태
}
