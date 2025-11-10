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
import com.equip.equiprental.equipment.service.iface.EquipmentItemService;
import com.equip.equiprental.member.domain.Member;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class EquipmentItemServiceImpl implements EquipmentItemService {

    private final EquipmentItemRepository equipmentItemRepository;
    private final EquipmentItemHistoryRepository equipmentItemHistoryRepository;

    @Override
    @Transactional
    public void updateItemStatus(UpdateItemStatusDto dto, Member changer) {
        EquipmentStatus newStatus = dto.getNewStatus();

        EquipmentItem item = equipmentItemRepository.findById(dto.getEquipmentItemId())
                .orElseThrow(() -> new CustomException(ErrorType.NOT_FOUND, "해당 정보로 등록된 기자재 아이템이 존재하지 않습니다."));

        EquipmentStatus oldStatus = item.getStatus();

        // 상태가 바뀌지 않았으면 아무 처리도 안 함
        if (oldStatus == newStatus) {
            return;
        }

        // UI에서 직접 변경일 경우 제한
        if (dto.isAdminChange()) {
            if (oldStatus == EquipmentStatus.RENTED) {
                throw new CustomException(ErrorType.CONFLICT, "해당 메뉴에서는 변경할 수 없습니다. 대여 관리 메뉴를 이용해주세요.");
            }
            if (newStatus == EquipmentStatus.RENTED) {
                throw new CustomException(ErrorType.CONFLICT, "해당 메뉴에서는 변경할 수 없습니다. 대여 관리 메뉴를 이용해주세요.");
            }
        }

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
