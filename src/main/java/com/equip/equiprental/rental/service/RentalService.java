package com.equip.equiprental.rental.service;

import com.equip.equiprental.common.dto.PageResponseDto;
import com.equip.equiprental.common.dto.SearchParamDto;
import com.equip.equiprental.rental.dto.*;

public interface RentalService {
    RentalResponseDto requestRental(RentalRequestDto dto, Long memberId);

    PageResponseDto<AdminRentalDto> getAdminRentalList(SearchParamDto paramDto);

    PageResponseDto<UserRentalDto> getUserRentalList(SearchParamDto paramDto, Long memberId);

    void updateRentalStatus(UpdateRentalStatusDto dto, Long rentalId, Long memberId);

    PageResponseDto<UserRentalItemDto> getUserRentalItemList(SearchParamDto paramDto, Long rentalId, Long memberId);
}
