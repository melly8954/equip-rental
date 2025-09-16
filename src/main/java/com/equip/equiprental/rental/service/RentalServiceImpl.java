package com.equip.equiprental.rental.service;

import com.equip.equiprental.common.dto.PageResponseDto;
import com.equip.equiprental.common.dto.SearchParamDto;
import com.equip.equiprental.common.exception.CustomException;
import com.equip.equiprental.common.exception.ErrorType;
import com.equip.equiprental.equipment.domain.Equipment;
import com.equip.equiprental.equipment.domain.EquipmentItem;
import com.equip.equiprental.equipment.domain.EquipmentItemHistory;
import com.equip.equiprental.equipment.domain.EquipmentStatus;
import com.equip.equiprental.equipment.repository.EquipmentItemHistoryRepository;
import com.equip.equiprental.equipment.repository.EquipmentItemRepository;
import com.equip.equiprental.equipment.repository.EquipmentRepository;
import com.equip.equiprental.member.domain.Member;
import com.equip.equiprental.member.repository.MemberRepository;
import com.equip.equiprental.rental.domain.Rental;
import com.equip.equiprental.rental.domain.RentalItem;
import com.equip.equiprental.rental.domain.RentalStatus;
import com.equip.equiprental.rental.dto.*;
import com.equip.equiprental.rental.repository.RentalItemRepository;
import com.equip.equiprental.rental.repository.RentalRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.IntStream;

@Service
@RequiredArgsConstructor
public class RentalServiceImpl implements RentalService{

    private final MemberRepository memberRepository;
    private final EquipmentRepository equipmentRepository;
    private final EquipmentItemRepository equipmentItemRepository;
    private final EquipmentItemHistoryRepository equipmentItemHistoryRepository;
    private final RentalRepository rentalRepository;
    private final RentalItemRepository rentalItemRepository;

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

    @Override
    @Transactional
    public void updateRentalStatus(UpdateRentalStatusDto dto, Long rentalId, Long memberId) {
        Rental rental = rentalRepository.findById(rentalId)
                .orElseThrow(() -> new CustomException(ErrorType.RENTAL_NOT_FOUND));

        rental.updateStatus(dto.getRentalStatusEnum());

        // 거절(REJECTED) 처리
        if (dto.getRentalStatusEnum() == RentalStatus.REJECTED) {
            return;
        }

        // 승인(APPROVED) 처리
        Pageable limit = PageRequest.of(0, rental.getQuantity());

        List<EquipmentItem> equipmentItems = equipmentItemRepository.findAvailableItemsForUpdate(dto.getEquipmentId(),limit);

        if (equipmentItems.size() < rental.getQuantity()) {
            throw new CustomException(ErrorType.EQUIPMENT_ITEM_INSUFFICIENT_STOCK);
        }

        List<Long> itemIds = equipmentItems.stream()
                .map(EquipmentItem::getEquipmentItemId)
                .toList();



        // bulk update 전에 상태 저장
        List<EquipmentStatus> oldStatuses = equipmentItems.stream()
                .map(EquipmentItem::getStatus)
                .toList();

        int updatedCount = equipmentItemRepository.approveRental(itemIds);
        if (updatedCount != itemIds.size()) {
            throw new CustomException(ErrorType.PARTIAL_UPDATE);
        }

        List<RentalItem> rentalItems = equipmentItems.stream()
                .map(item -> RentalItem.builder()
                        .rental(rental)
                        .equipmentItem(item)
                        .startDate(rental.getRequestStartDate())
                        .endDate(rental.getRequestEndDate())
                        .actualReturnDate(null)
                        .isExtended(false)
                        .build())
                .toList();
        rentalItemRepository.saveAll(rentalItems);

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(ErrorType.USER_NOT_FOUND));

        List<EquipmentItemHistory> histories = IntStream.range(0, equipmentItems.size())
                .mapToObj(i -> EquipmentItemHistory.builder()
                        .item(equipmentItems.get(i))
                        .changedBy(member)
                        .oldStatus(oldStatuses.get(i))
                        .newStatus(EquipmentStatus.RENTED)
                        .build())
                .toList();
        equipmentItemHistoryRepository.saveAll(histories);
    }
}
