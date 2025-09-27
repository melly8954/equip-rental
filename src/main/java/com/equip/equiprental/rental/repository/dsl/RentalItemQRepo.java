package com.equip.equiprental.rental.repository.dsl;

import com.equip.equiprental.common.dto.SearchParamDto;
import com.equip.equiprental.rental.dto.AdminRentalItemDto;
import com.equip.equiprental.rental.dto.ReturnedRentalItemDto;
import com.equip.equiprental.rental.dto.UserRentalItemDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface RentalItemQRepo {
    Page<AdminRentalItemDto> findAdminRentalItems(SearchParamDto paramDto, Pageable pageable);
    Page<UserRentalItemDto> findUserRentalItems(SearchParamDto paramDto, Pageable pageable, Long rentalId, Long memberId);
    Page<ReturnedRentalItemDto> findReturnRentalItems(SearchParamDto paramDto, Pageable pageable, Long rentalId, Long memberId);
}
