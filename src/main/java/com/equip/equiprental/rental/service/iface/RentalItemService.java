package com.equip.equiprental.rental.service.iface;

import com.equip.equiprental.common.dto.PageResponseDto;
import com.equip.equiprental.common.dto.SearchParamDto;
import com.equip.equiprental.rental.dto.AdminRentalItemDto;
import com.equip.equiprental.rental.dto.ExtendRentalItemDto;

public interface RentalItemService {
    PageResponseDto<AdminRentalItemDto> getAdminRentalItemLists(SearchParamDto paramDto);

    void extendRentalItem(Long rentalItem, ExtendRentalItemDto dto);

    void returnRentalItem(Long rentalItem, Long memberId);

    void updateOverdueStatus();
}
