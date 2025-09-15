package com.equip.equiprental.rental.service;

import com.equip.equiprental.common.dto.PageResponseDto;
import com.equip.equiprental.common.dto.SearchParamDto;
import com.equip.equiprental.common.exception.CustomException;
import com.equip.equiprental.common.exception.ErrorType;
import com.equip.equiprental.equipment.domain.Equipment;
import com.equip.equiprental.equipment.repository.EquipmentItemRepository;
import com.equip.equiprental.equipment.repository.EquipmentRepository;
import com.equip.equiprental.member.domain.Member;
import com.equip.equiprental.member.repository.MemberRepository;
import com.equip.equiprental.rental.domain.Rental;
import com.equip.equiprental.rental.domain.RentalStatus;
import com.equip.equiprental.rental.dto.AdminRentalDto;
import com.equip.equiprental.rental.dto.RentalRequestDto;
import com.equip.equiprental.rental.dto.RentalResponseDto;
import com.equip.equiprental.rental.dto.UserRentalDto;
import com.equip.equiprental.rental.repository.RentalRepository;
import lombok.RequiredArgsConstructor;
import org.apache.catalina.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class RentalServiceImpl implements RentalService{

    private final MemberRepository memberRepository;
    private final EquipmentRepository equipmentRepository;
    private final EquipmentItemRepository equipmentItemRepository;
    private final RentalRepository rentalRepository;

    @Override
    @Transactional
    public RentalResponseDto requestRental(RentalRequestDto dto, Long memberId) {
        Member member = memberRepository.findByMemberId(memberId)
                .orElseThrow(() -> new CustomException(ErrorType.USER_NOT_FOUND));

        Equipment equipment = equipmentRepository.findById(dto.getEquipmentId())
                .orElseThrow(() -> new CustomException(ErrorType.EQUIPMENT_NOT_FOUND));

        int availableStock = equipmentItemRepository.countAvailableByEquipmentId(dto.getEquipmentId());

        // 대여 날짜 검증
        LocalDate today = LocalDate.now();
        if (dto.getStartDate().isBefore(today)) {
            throw new CustomException(ErrorType.INVALID_RENTAL_START_DATE);
        }

        if (dto.getEndDate().isBefore(dto.getStartDate())) {
            throw new CustomException(ErrorType.INVALID_RENTAL_END_DATE);
        }

        // 수량 검증
        if (dto.getQuantity() <= 0 || dto.getQuantity() > availableStock) {
            throw new CustomException(ErrorType.INVALID_RENTAL_QUANTITY);
        }

        Rental rental = Rental.builder()
                .member(member)
                .equipment(equipment)
                .requestStartDate(dto.getStartDate())
                .requestEndDate(dto.getEndDate())
                .quantity(dto.getQuantity())
                .rentalReason(dto.getRentalReason())
                .status(RentalStatus.PENDING)
                .build();

        rentalRepository.save(rental);

        return RentalResponseDto.builder()
                .rentalId(rental.getRentalId())
                .equipmentId(equipment.getEquipmentId())
                .quantity(rental.getQuantity())
                .status(rental.getStatus())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponseDto<AdminRentalDto> getAdminRentalList(SearchParamDto paramDto) {
        Pageable pageable = paramDto.getPageable();

        Page<AdminRentalDto> dtosPage = rentalRepository.findAdminRentals(paramDto, pageable);

        return PageResponseDto.<AdminRentalDto>builder()
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
    public PageResponseDto<UserRentalDto> getUserRentalList(SearchParamDto paramDto, Long memberId) {
        Pageable pageable = paramDto.getPageable();

        Page<UserRentalDto> dtosPage = rentalRepository.findUserRentals(paramDto, pageable, memberId);

        return PageResponseDto.<UserRentalDto>builder()
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
