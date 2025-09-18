package com.equip.equiprental.rental.service;

import com.equip.equiprental.common.dto.PageResponseDto;
import com.equip.equiprental.common.dto.SearchParamDto;
import com.equip.equiprental.common.exception.CustomException;
import com.equip.equiprental.common.exception.ErrorType;
import com.equip.equiprental.rental.domain.RentalItem;
import com.equip.equiprental.rental.dto.AdminRentalItemDto;
import com.equip.equiprental.rental.dto.ExtendRequestDto;
import com.equip.equiprental.rental.dto.UserRentalItemDto;
import com.equip.equiprental.rental.repository.RentalItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class RentalItemServiceImpl implements RentalItemService{

    private final RentalItemRepository rentalItemRepository;

    @Override
    @Transactional(readOnly = true)
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

    @Override
    @Transactional(readOnly = true)
    public PageResponseDto<UserRentalItemDto> getUserRentalItemLists(SearchParamDto paramDto, Long memberId) {
        Pageable pageable = paramDto.getPageable();

        Page<UserRentalItemDto> dtosPage = rentalItemRepository.findUserRentalItems(paramDto, pageable, memberId);

        // overdue 계산
        dtosPage.getContent().forEach(dto -> {
            boolean overdue = dto.getActualReturnDate() == null
                    && dto.getEndDate() != null
                    && dto.getEndDate().isBefore(LocalDate.now());
            dto.setOverdue(overdue);
        });

        return PageResponseDto.<UserRentalItemDto>builder()
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

    @Override
    @Transactional
    public void extendRentalItem(Long rentalItem, ExtendRequestDto dto) {
        RentalItem item = rentalItemRepository.findById(rentalItem)
                .orElseThrow(() -> new CustomException(ErrorType.RENTAL_NOT_FOUND));

        LocalDate extendedEndDate = item.getEndDate().plusDays(dto.getDays());

        // 엔티티 수정 메서드 호출
        item.extend(extendedEndDate);
    }
}
