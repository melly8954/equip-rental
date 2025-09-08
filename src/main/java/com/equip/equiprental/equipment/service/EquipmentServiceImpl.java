package com.equip.equiprental.equipment.service;


import com.equip.equiprental.equipment.domain.Equipment;
import com.equip.equiprental.equipment.dto.EquipmentRegisterRequest;
import com.equip.equiprental.equipment.dto.EquipmentRegisterResponse;
import com.equip.equiprental.equipment.repository.EquipmentRepository;
import com.equip.equiprental.filestorage.domain.FileMeta;
import com.equip.equiprental.filestorage.repository.FileRepository;
import com.equip.equiprental.filestorage.service.FileService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class EquipmentServiceImpl implements EquipmentService {
    private final EquipmentRepository equipmentRepository;
    private final FileRepository fileRepository;
    private final FileService fileService;

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

        List<FileMeta> savedFiles = new ArrayList<>();

        if(files != null && !files.isEmpty()) {
            int fileOrder = 0;
            List<String> fileUrls = fileService.saveFiles(files, "equipment");

            for (int i = 0; i < files.size(); i++) {
                MultipartFile file = files.get(i);
                String url = fileUrls.get(i); // fileService에서 생성한 접근 URL

                FileMeta meta = FileMeta.builder()
                        .relatedType("equipment")
                        .relatedId(equipment.getEquipmentId())
                        .originalName(file.getOriginalFilename())
                        .uniqueName(url.substring(url.lastIndexOf("/") + 1)) // URL 에서 uniqueName 추출
                        .fileOrder(fileOrder++)
                        .fileType(file.getContentType())
                        .filePath(url) // 접근 URL
                        .fileSize(file.getSize())
                        .build();

                savedFiles.add(meta);
            }

            fileRepository.saveAll(savedFiles);
        }

        List<EquipmentRegisterResponse.EquipmentImage> lists = new ArrayList<>();
        for (FileMeta meta : savedFiles) {
            lists.add(EquipmentRegisterResponse.EquipmentImage.builder()
                    .fileId(meta.getFileId())
                    .originalName(meta.getOriginalName())
                    .url(meta.getFilePath())
                    .fileOrder(meta.getFileOrder())
                    .fileType(meta.getFileType())
                    .fileSize(meta.getFileSize())
                    .build());
        }

        return EquipmentRegisterResponse.builder()
                .equipmentId(equipment.getEquipmentId())
                .category(equipment.getCategory().name())
                .subCategory(equipment.getSubCategory())
                .model(equipment.getModel())
                .stock(equipment.getStock())
                .images(lists)
                .createdAt(equipment.getCreatedAt())
                .updatedAt(equipment.getUpdatedAt())
                .build();
    }
}
