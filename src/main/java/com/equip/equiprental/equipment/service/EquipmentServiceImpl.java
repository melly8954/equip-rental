package com.equip.equiprental.equipment.service;


import com.equip.equiprental.common.dto.PageResponseDto;
import com.equip.equiprental.common.dto.SearchParamDto;
import com.equip.equiprental.common.exception.CustomException;
import com.equip.equiprental.common.exception.ErrorType;
import com.equip.equiprental.equipment.domain.Equipment;
import com.equip.equiprental.equipment.domain.EquipmentCategory;
import com.equip.equiprental.equipment.domain.EquipmentItem;
import com.equip.equiprental.equipment.domain.EquipmentStatus;
import com.equip.equiprental.equipment.dto.EquipmentDto;
import com.equip.equiprental.equipment.dto.EquipmentRegisterRequest;
import com.equip.equiprental.equipment.dto.EquipmentRegisterResponse;
import com.equip.equiprental.equipment.repository.EquipmentItemRepository;
import com.equip.equiprental.equipment.repository.EquipmentRepository;
import com.equip.equiprental.equipment.util.ModelCodeGenerator;
import com.equip.equiprental.filestorage.domain.FileMeta;
import com.equip.equiprental.filestorage.repository.FileRepository;
import com.equip.equiprental.filestorage.service.FileService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EquipmentServiceImpl implements EquipmentService {
    private final EquipmentRepository equipmentRepository;
    private final EquipmentItemRepository equipmentItemRepository;
    private final ModelCodeGenerator modelCodeGenerator;
    private final FileRepository fileRepository;
    private final FileService fileService;

    @Override
    @Transactional
    public EquipmentRegisterResponse register(EquipmentRegisterRequest dto, List<MultipartFile> files) {
        // 모델이 이미 존재하면 예외 던지기
        if (equipmentRepository.findByModel(dto.getModel()).isPresent()) {
            throw new CustomException(ErrorType.EXIST_EQUIPMENT_MODEL_CODE);
        }
        // 없으면 새 장비 생성
        String modelCode = modelCodeGenerator.generate();

        Equipment equipment = Equipment.builder()
                .category(dto.getCategoryEnum())
                .subCategory(dto.getSubCategory())
                .model(dto.getModel())
                .modelCode(modelCode)
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

        // EquipmentItem 생성 (stock 수량만큼)
        long baseSequence = equipmentRepository.countByModel(equipment.getModel())-1; // 방금 생성한 장비를 제외하고 기존 수량만 가져오기
        List<EquipmentItem> items = new ArrayList<>();

        for (int i = 0; i < equipment.getStock(); i++) {
            long sequence = baseSequence + i + 1; // 반복마다 시퀀스 증가
            String serialNumber = generateSerialNumber(equipment.getModelCode(), sequence);

            EquipmentItem item = EquipmentItem.builder()
                    .equipment(equipment)
                    .serialNumber(serialNumber)
                    .status(EquipmentStatus.AVAILABLE) // 초기 상태
                    .build();

            items.add(item);
        }
        equipmentItemRepository.saveAll(items);

        // 응답 객체 생성
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

    @Override
    public PageResponseDto<EquipmentDto> getEquipment(SearchParamDto paramDto) {
        Pageable pageable = paramDto.getPageable();
        EquipmentCategory categoryEnum = paramDto.getCategoryEnum();

        Page<Equipment> page = equipmentRepository.findByFilters(
                categoryEnum,
                paramDto.getSubCategory(),
                paramDto.getModel(),
                pageable
        );

        List<EquipmentDto> content = page.getContent().stream()
                .map(e -> {
                    List<String> urls = fileRepository.findUrlsByEquipmentId(e.getEquipmentId());
                    String imageUrl = urls.isEmpty() ? null : urls.get(0); // 첫 번째 이미지 사용

                    int availableStock = equipmentItemRepository.countByStatus(e.getEquipmentId(), EquipmentStatus.AVAILABLE);

                    return EquipmentDto.builder()
                            .equipmentId(e.getEquipmentId())
                            .category(e.getCategory().name())
                            .subCategory(e.getSubCategory())
                            .model(e.getModel())
                            .stock(availableStock)
                            .imageUrl(imageUrl)
                            .build();
                })
                .collect(Collectors.toList());

        return PageResponseDto.<EquipmentDto>builder()
                .content(content)
                .page(page.getNumber() + 1)
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .numberOfElements(page.getNumberOfElements())
                .first(page.isFirst())
                .last(page.isLast())
                .empty(page.isEmpty())
                .build();
    }

    private String generateSerialNumber(String modelCode, long sequence) {
        // 랜덤 4자리 숫자 생성
        int random4 = new Random().nextInt(10000); // 0~9999
        String random4Str = String.format("%04d", random4); // 4자리 고정, 앞에 0 채우기

        return modelCode + "-" + sequence + "-" + random4Str;
    }
}
