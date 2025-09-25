package com.equip.equiprental.equipment.service.iface;

import com.equip.equiprental.common.dto.PageResponseDto;
import com.equip.equiprental.common.dto.SearchParamDto;
import com.equip.equiprental.equipment.dto.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface EquipmentService {
    EquipmentRegisterResponse register(EquipmentRegisterRequest dto, List<MultipartFile> files);

    PageResponseDto<EquipmentDto> getEquipment(SearchParamDto paramDto);
    EquipmentItemListDto getEquipmentItem(Long equipmentId, SearchParamDto paramDto);

    void increaseStock(Long equipmentId, IncreaseStockRequestDto dto);

    void updateEquipmentImage(Long equipmentId, List<MultipartFile> files);
}
