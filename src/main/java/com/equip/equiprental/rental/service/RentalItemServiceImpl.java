package com.equip.equiprental.rental.service;

import com.equip.equiprental.common.dto.PageResponseDto;
import com.equip.equiprental.common.dto.SearchParamDto;
import com.equip.equiprental.common.exception.CustomException;
import com.equip.equiprental.common.exception.ErrorType;
import com.equip.equiprental.equipment.domain.EquipmentItem;
import com.equip.equiprental.equipment.domain.EquipmentItemHistory;
import com.equip.equiprental.equipment.domain.EquipmentStatus;
import com.equip.equiprental.equipment.repository.EquipmentItemHistoryRepository;
import com.equip.equiprental.member.domain.Member;
import com.equip.equiprental.member.repository.MemberRepository;
import com.equip.equiprental.rental.domain.RentalItem;
import com.equip.equiprental.rental.dto.AdminRentalItemDto;
import com.equip.equiprental.rental.dto.ExtendRentalItemDto;
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
    private final EquipmentItemHistoryRepository equipmentItemHistoryRepository;
    private final MemberRepository memberRepository;

    @Override
    @Transactional(readOnly = true)
    public PageResponseDto<AdminRentalItemDto> getAdminRentalItemLists(SearchParamDto paramDto) {
        Pageable pageable = paramDto.getPageable();

        Page<AdminRentalItemDto> dtosPage = rentalItemRepository.findAdminRentalItems(paramDto, pageable);

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
    @Transactional
    public void extendRentalItem(Long rentalItem, ExtendRentalItemDto dto) {
        RentalItem item = rentalItemRepository.findById(rentalItem)
                .orElseThrow(() -> new CustomException(ErrorType.RENTAL_NOT_FOUND));

        LocalDate extendedEndDate = item.getEndDate().plusDays(dto.getDays());

        // 엔티티 수정 메서드 호출
        item.extend(extendedEndDate);
    }

    @Override
    @Transactional
    public void returnRentalItem(Long rentalItem, Long memberId) {
        RentalItem item = rentalItemRepository.findById(rentalItem)
                .orElseThrow(() -> new CustomException(ErrorType.RENTAL_NOT_FOUND));

        EquipmentItem equipmentItem = item.getEquipmentItem();

        EquipmentStatus oldStatus = equipmentItem.getStatus();

        // 장비 상태 변경(사용 가능) + 실 반납일 저장
        equipmentItem.updateStatus(EquipmentStatus.AVAILABLE);
        item.returnItem(LocalDate.now());

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(ErrorType.USER_NOT_FOUND));

        EquipmentItemHistory history = EquipmentItemHistory.builder()
                .item(equipmentItem)
                .changedBy(member)
                .oldStatus(oldStatus)
                .newStatus(EquipmentStatus.AVAILABLE)
                .build();

        equipmentItemHistoryRepository.save(history);
    }

    @Override
    @Transactional
    public int updateOverdueStatus() {
        return rentalItemRepository.markOverdueRentalItems();
    }
}
