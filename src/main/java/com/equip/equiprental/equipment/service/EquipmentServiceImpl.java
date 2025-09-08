package com.equip.equiprental.equipment.service;

import com.equip.equiprental.equipment.domain.Equipment;
import com.equip.equiprental.equipment.dto.EquipmentRegisterRequest;
import com.equip.equiprental.equipment.dto.EquipmentRegisterResponse;
import com.equip.equiprental.equipment.repository.EquipmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EquipmentServiceImpl implements EquipmentService {
    private final EquipmentRepository equipmentRepository;

    @Override
    @Transactional
    public EquipmentRegisterResponse register(EquipmentRegisterRequest dto, List<MultipartFile> files) {
        Equipment equipment = Equipment.builder()
                .category(dto.getCategoryEnum())
                .subCategory(dto.getSubCategory())
                .model(dto.getModel())
                .stock(dto.getStock())
                .build();
        equipmentRepository.save(equipment);
        
        return null;
    }
}
