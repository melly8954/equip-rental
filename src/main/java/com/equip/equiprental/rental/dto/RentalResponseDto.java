package com.equip.equiprental.rental.dto;

import com.equip.equiprental.rental.domain.RentalStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
@Builder
public class RentalResponseDto {
    private Long rentalId;                       // 대여 신청 ID
    private Long equipmentId;                    // 장비 모델 ID
    private List<Long> equipmentItemIds;        // 할당된 장비 아이템 ID 목록
    private RentalStatus status;                 // 대여 신청 상태
}
