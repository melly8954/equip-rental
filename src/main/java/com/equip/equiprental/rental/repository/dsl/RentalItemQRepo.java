package com.equip.equiprental.rental.repository.dsl;

import com.equip.equiprental.common.dto.SearchParamDto;
import com.equip.equiprental.rental.dto.AdminRentalItemDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface RentalItemQRepo {
    Page<AdminRentalItemDto> findAdminRentalItems(SearchParamDto paramDto, Pageable pageable);
}
