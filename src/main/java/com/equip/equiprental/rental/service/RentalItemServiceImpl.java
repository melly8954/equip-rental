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
import com.equip.equiprental.rental.domain.*;
import com.equip.equiprental.rental.dto.AdminRentalItemDto;
import com.equip.equiprental.rental.dto.ExtendRentalItemDto;
import com.equip.equiprental.rental.repository.RentalItemOverdueRepository;
import com.equip.equiprental.rental.repository.RentalItemRepository;
import com.equip.equiprental.rental.service.iface.RentalItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RentalItemServiceImpl implements RentalItemService {

    private final RentalItemRepository rentalItemRepository;
    private final RentalItemOverdueRepository rentalItemOverdueRepository;
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
        Rental rental = item.getRental();

        EquipmentStatus oldStatus = equipmentItem.getStatus();

        // 장비 상태 변경(사용 가능) + 실 반납일 저장
        equipmentItem.updateStatus(EquipmentStatus.AVAILABLE);
        item.returnItem(LocalDate.now());

        // 연체 여부 계산 후 연체 테이블 생성
        long overdueDays = ChronoUnit.DAYS.between(item.getEndDate(), LocalDate.now());
        if (overdueDays > 0) {
            RentalItemOverdue overdue = RentalItemOverdue.builder()
                    .rentalItem(item)
                    .plannedEndDate(item.getEndDate())
                    .actualReturnDate(LocalDate.now())
                    .overdueDays((int) overdueDays)
                    .build();
            rentalItemOverdueRepository.save(overdue);
        }

        // 모든 아이템이 반납되었으면 Rental 상태 변경
        List<RentalItem> rentalItems = rentalItemRepository.findByRental_RentalId(rental.getRentalId());

        boolean allReturned = rentalItems.stream()
                .allMatch(i -> i.getStatus() == RentalItemStatus.RETURNED);

        if (allReturned) {
            rental.updateStatus(RentalStatus.COMPLETED);
        }

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(ErrorType.USER_NOT_FOUND));

        EquipmentItemHistory history = EquipmentItemHistory.builder()
                .item(equipmentItem)
                .changedBy(member)
                .oldStatus(oldStatus)
                .newStatus(EquipmentStatus.AVAILABLE)
                .rentalStartDate(item.getStartDate())
                .actualReturnDate(item.getActualReturnDate())
                .rentedUser(item.getRental().getMember())
                .build();

        equipmentItemHistoryRepository.save(history);
    }

    @Override
    @Transactional
    public int updateOverdueStatus() {
        return rentalItemRepository.markOverdueRentalItems();
    }
}
