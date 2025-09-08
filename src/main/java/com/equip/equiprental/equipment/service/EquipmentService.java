package com.equip.equiprental.equipment.service;

import com.equip.equiprental.common.dto.PageResponseDto;
import com.equip.equiprental.common.dto.SearchParamDto;
import com.equip.equiprental.equipment.dto.EquipmentDto;
import com.equip.equiprental.equipment.dto.EquipmentRegisterRequest;
import com.equip.equiprental.equipment.dto.EquipmentRegisterResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface EquipmentService {
    EquipmentRegisterResponse register(EquipmentRegisterRequest dto, List<MultipartFile> files);

    PageResponseDto<EquipmentDto> getEquipment(SearchParamDto paramDto);

}
