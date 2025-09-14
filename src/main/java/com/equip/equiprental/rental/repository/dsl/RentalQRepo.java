package com.equip.equiprental.rental.repository.dsl;

import com.equip.equiprental.common.dto.SearchParamDto;
import com.equip.equiprental.rental.dto.AdminRentalDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface RentalQRepo {
    Page<AdminRentalDto> findAdminRentals(SearchParamDto paramDto, Pageable pageable);
}
