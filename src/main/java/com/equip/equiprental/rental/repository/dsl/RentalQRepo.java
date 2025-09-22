package com.equip.equiprental.rental.repository.dsl;

import com.equip.equiprental.common.dto.SearchParamDto;
import com.equip.equiprental.rental.dto.AdminRentalDto;
import com.equip.equiprental.rental.dto.UserRentalDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface RentalQRepo {
    Page<AdminRentalDto> findAdminRentals(SearchParamDto paramDto, Pageable pageable);
    Page<UserRentalDto> findUserRentals(SearchParamDto paramDto, Pageable pageable, Long memberId);
}
