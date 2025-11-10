package com.equip.equiprental.rental.service.iface;

import com.equip.equiprental.common.dto.PageResponseDto;
import com.equip.equiprental.rental.dto.AdminRentalItemDto;
import com.equip.equiprental.rental.dto.ExtendRentalItemDto;
import com.equip.equiprental.rental.dto.RentalFilter;

public interface RentalItemService {
    PageResponseDto<AdminRentalItemDto> getAdminRentalItemLists(RentalFilter paramDto);

    void extendRentalItem(Long rentalItem, ExtendRentalItemDto dto, Long memberId);

    void returnRentalItem(Long rentalItem, Long memberId);

    void updateOverdueStatus();
}
