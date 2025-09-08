package com.equip.equiprental.equipment.service;

import com.equip.equiprental.equipment.dto.EquipmentRegisterRequest;
import com.equip.equiprental.equipment.dto.EquipmentRegisterResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface EquipmentService {
    EquipmentRegisterResponse register(EquipmentRegisterRequest dto, List<MultipartFile> files);
}
