package com.equip.equiprental.equipment.service;

import com.equip.equiprental.common.dto.PageResponseDto;
import com.equip.equiprental.common.dto.SearchParamDto;
import com.equip.equiprental.common.exception.CustomException;
import com.equip.equiprental.common.exception.ErrorType;
import com.equip.equiprental.equipment.domain.EquipmentItem;
import com.equip.equiprental.equipment.domain.EquipmentItemHistory;
import com.equip.equiprental.equipment.domain.EquipmentStatus;
import com.equip.equiprental.equipment.dto.EquipmentItemHistoryDto;
import com.equip.equiprental.equipment.dto.UpdateItemStatusDto;
import com.equip.equiprental.equipment.repository.EquipmentItemHistoryRepository;
import com.equip.equiprental.equipment.repository.EquipmentItemRepository;
import com.equip.equiprental.member.domain.Member;
import com.equip.equiprental.rental.domain.RentalItem;
import com.equip.equiprental.rental.repository.RentalItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class EquipmentItemServiceImpl implements EquipmentItemService{

    private final EquipmentItemRepository equipmentItemRepository;
    private final EquipmentItemHistoryRepository equipmentItemHistoryRepository;
    private final RentalItemRepository rentalItemRepository;

    @Override
    @Transactional
    public void updateItemStatus(UpdateItemStatusDto dto, Member changer) {
        EquipmentStatus newStatus = dto.getEquipmentItemStatusEnum();

        EquipmentItem item = equipmentItemRepository.findById(dto.getEquipmentItemId())
                .orElseThrow(() -> new CustomException(ErrorType.EQUIPMENT_ITEM_NOT_FOUND));

        EquipmentStatus oldStatus = item.getStatus();

        // 상태 변경
        item.updateStatus(newStatus);

        EquipmentItemHistory history = EquipmentItemHistory.builder()
                .item(item)
                .changedBy(changer)
                .oldStatus(oldStatus)
                .newStatus(newStatus)
                .build();

        equipmentItemHistoryRepository.save(history);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponseDto<EquipmentItemHistoryDto> getItemHistory(Long equipmentItemId, SearchParamDto paramDto) {
        Pageable pageable = paramDto.getPageable();

        Page<EquipmentItemHistoryDto> historyDtosPage = equipmentItemHistoryRepository.findHistoriesByEquipmentItemId(equipmentItemId, pageable);

        for (EquipmentItemHistoryDto dto : historyDtosPage) {
            if ("RENTED".equals(dto.getNewStatus())) {
                RentalItem rentalItem = rentalItemRepository
                        .findFirstByEquipmentItem_EquipmentItemIdAndActualReturnDateIsNull(equipmentItemId);

                if (rentalItem != null) {
                    dto.setCurrentOwnerName(rentalItem.getRental().getMember().getName());
                    dto.setCurrentOwnerDept(rentalItem.getRental().getMember().getDepartment());
                } else {
                    dto.setCurrentOwnerName("관리자");
                    dto.setCurrentOwnerDept("시스템");
                }
            } else {
                dto.setCurrentOwnerName("관리자");
                dto.setCurrentOwnerDept("시스템");
            }
        }

        return PageResponseDto.<EquipmentItemHistoryDto>builder()
                .content(historyDtosPage.getContent())
                .page(historyDtosPage.getNumber() + 1)
                .size(historyDtosPage.getSize())
                .totalElements(historyDtosPage.getTotalElements())
                .totalPages(historyDtosPage.getTotalPages())
                .numberOfElements(historyDtosPage.getNumberOfElements())
                .first(historyDtosPage.isFirst())
                .last(historyDtosPage.isLast())
                .empty(historyDtosPage.isEmpty())
                .build();
    }
}
