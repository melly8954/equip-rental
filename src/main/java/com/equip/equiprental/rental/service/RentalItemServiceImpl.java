package com.equip.equiprental.rental.service;

import com.equip.equiprental.common.dto.PageResponseDto;
import com.equip.equiprental.common.dto.SearchParamDto;
import com.equip.equiprental.rental.dto.AdminRentalItemDto;
import com.equip.equiprental.rental.repository.RentalItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class RentalItemServiceImpl implements RentalItemService{

    private final RentalItemRepository rentalItemRepository;

    @Override
    public PageResponseDto<AdminRentalItemDto> getAdminRentalItemLists(SearchParamDto paramDto) {
        Pageable pageable = paramDto.getPageable();

        Page<AdminRentalItemDto> dtosPage = rentalItemRepository.findAdminRentalItems(paramDto, pageable);

        // overdue 계산
        dtosPage.getContent().forEach(dto -> {
            boolean overdue = dto.getActualReturnDate() == null
                    && dto.getEndDate() != null
                    && dto.getEndDate().isBefore(LocalDate.now());
            dto.setOverdue(overdue);
        });

        return PageResponseDto.<AdminRentalItemDto>builder()
                .content(dtosPage.getContent())
                .page(dtosPage.getNumber() + 1)
                .size(dtosPage.getSize())
                .totalElements(dtosPage.getTotalElements())
                .totalPages(dtosPage.getTotalPages())
                .numberOfElements(dtosPage.getNumberOfElements())
                .first(dtosPage.isFirst())
                .last(dtosPage.isLast())
                .empty(dtosPage.isEmpty())
                .build();
    }
}
