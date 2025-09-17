package com.equip.equiprental.rental.service;

import com.equip.equiprental.common.dto.PageResponseDto;
import com.equip.equiprental.common.dto.SearchParamDto;
import com.equip.equiprental.rental.dto.AdminRentalItemDto;

public interface RentalItemService {
    PageResponseDto<AdminRentalItemDto> getAdminRentalItemLists(SearchParamDto paramDto);
}
