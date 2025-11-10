package com.equip.equiprental.rental.repository.dsl;

import com.equip.equiprental.rental.dto.AdminRentalDto;
import com.equip.equiprental.rental.dto.RentalFilter;
import com.equip.equiprental.rental.dto.UserRentalDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface RentalQRepo {
    Page<AdminRentalDto> findAdminRentals(RentalFilter paramDto, Pageable pageable);
    Page<UserRentalDto> findUserRentals(RentalFilter paramDto, Pageable pageable, Long currentUserId);
}
