package com.equip.equiprental.equipment.service.iface;

import com.equip.equiprental.common.dto.PageResponseDto;
import com.equip.equiprental.equipment.dto.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface EquipmentService {
    EquipmentRegisterResponse register(EquipmentRegisterRequest dto, List<MultipartFile> files);

    PageResponseDto<EquipmentDto> getEquipment(EquipmentFilter paramDto);
    EquipmentItemListDto getEquipmentItem(Long equipmentId, EquipmentStatusFilter paramDto);

    void increaseStock(Long equipmentId, IncreaseStockRequestDto dto);

    void updateEquipmentImage(Long equipmentId, List<MultipartFile> files);

    void softDeleteEquip(Long equipmentId);
}
