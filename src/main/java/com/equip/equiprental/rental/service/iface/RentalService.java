package com.equip.equiprental.rental.service.iface;

import com.equip.equiprental.common.dto.PageResponseDto;
import com.equip.equiprental.common.dto.SearchParamDto;
import com.equip.equiprental.rental.dto.*;

public interface RentalService {
    RentalResponseDto requestRental(RentalRequestDto dto, Long currentUserId);

    PageResponseDto<AdminRentalDto> getAdminRentalList(RentalFilter paramDto);

    PageResponseDto<UserRentalDto> getUserRentalList(RentalFilter paramDto, Long currentUserId);

    void updateRentalStatus(UpdateRentalStatusDto dto, Long rentalId, Long memberId);

    PageResponseDto<UserRentalItemDto> getUserRentalItemList(SearchParamDto paramDto, Long rentalId, Long currentUserId);
    PageResponseDto<ReturnedRentalItemDto> getReturnRentalItemList(SearchParamDto paramDto, Long rentalId, Long currentUserId);
}
