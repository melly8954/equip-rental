package com.equip.equiprental.rental.service;

import com.equip.equiprental.common.dto.PageResponseDto;
import com.equip.equiprental.common.dto.SearchParamDto;
import com.equip.equiprental.rental.dto.AdminRentalDto;
import com.equip.equiprental.rental.dto.RentalRequestDto;
import com.equip.equiprental.rental.dto.RentalResponseDto;

public interface RentalService {
    RentalResponseDto requestRental(RentalRequestDto dto, Long memberId);

    PageResponseDto<AdminRentalDto> getAdminRentalList(SearchParamDto paramDto);
}
