package com.equip.equiprental.rental.service;

import com.equip.equiprental.rental.dto.RentalRequestDto;
import com.equip.equiprental.rental.dto.RentalResponseDto;

public interface RentalService {
    RentalResponseDto requestRental(RentalRequestDto dto, Long memberId);
}
