package com.equip.equiprental.rental.service;

import com.equip.equiprental.common.dto.PageResponseDto;
import com.equip.equiprental.common.dto.SearchParamDto;
import com.equip.equiprental.rental.dto.AdminRentalItemDto;
import com.equip.equiprental.rental.dto.ExtendRequestDto;
import com.equip.equiprental.rental.dto.UserRentalItemDto;

public interface RentalItemService {
    PageResponseDto<AdminRentalItemDto> getAdminRentalItemLists(SearchParamDto paramDto);
    PageResponseDto<UserRentalItemDto> getUserRentalItemLists(SearchParamDto paramDto, Long memberId);

    void extendRentalItem(Long rentalItem, ExtendRequestDto dto);
}
