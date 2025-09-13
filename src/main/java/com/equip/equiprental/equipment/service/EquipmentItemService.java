package com.equip.equiprental.equipment.service;

import com.equip.equiprental.common.dto.PageResponseDto;
import com.equip.equiprental.common.dto.SearchParamDto;
import com.equip.equiprental.equipment.dto.EquipmentItemHistoryDto;
import com.equip.equiprental.equipment.dto.UpdateItemStatusDto;
import com.equip.equiprental.member.domain.Member;

public interface EquipmentItemService {
    void updateItemStatus(UpdateItemStatusDto dto, Member changer);
    PageResponseDto<EquipmentItemHistoryDto> getItemHistory(Long equipmentItemId, SearchParamDto paramDto);
}
