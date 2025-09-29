package com.equip.equiprental.equipment.service;


import com.equip.equiprental.common.dto.PageResponseDto;
import com.equip.equiprental.common.dto.SearchParamDto;
import com.equip.equiprental.common.exception.CustomException;
import com.equip.equiprental.common.exception.ErrorType;
import com.equip.equiprental.equipment.domain.Equipment;
import com.equip.equiprental.equipment.domain.EquipmentItem;
import com.equip.equiprental.equipment.domain.EquipmentStatus;
import com.equip.equiprental.equipment.domain.SubCategory;
import com.equip.equiprental.equipment.dto.*;
import com.equip.equiprental.equipment.repository.EquipmentItemRepository;
import com.equip.equiprental.equipment.repository.EquipmentRepository;
import com.equip.equiprental.equipment.repository.SubCategoryRepository;
import com.equip.equiprental.equipment.service.iface.EquipmentService;
import com.equip.equiprental.filestorage.domain.FileMeta;
import com.equip.equiprental.filestorage.repository.FileRepository;
import com.equip.equiprental.filestorage.service.iface.FileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Slf4j
@Service
@RequiredArgsConstructor
public class EquipmentServiceImpl implements EquipmentService {
    private final SubCategoryRepository subCategoryRepository;
    private final EquipmentRepository equipmentRepository;
    private final EquipmentItemRepository equipmentItemRepository;
    private final FileRepository fileRepository;
    private final FileService fileService;

    @Override
    @Transactional
    public EquipmentRegisterResponse register(EquipmentRegisterRequest dto, List<MultipartFile> files) {
        SubCategory subCategory = subCategoryRepository.findById(dto.getSubCategoryId())
                .orElseThrow(() -> new CustomException(ErrorType.NOT_FOUND));

        // 모델이 이미 존재하면 예외 던지기
        if (equipmentRepository.findByModel(dto.getModel()).isPresent()) {
            throw new CustomException(ErrorType.EXIST_EQUIPMENT_MODEL_CODE);
        }
        // 없으면 새 장비 생성
        String categoryCode = subCategory.getCategory().getCategoryCode().substring(0, 2).toUpperCase();
        long modelSeq = equipmentRepository.findMaxModelSequence(subCategory.getSubCategoryId()).orElse(0L) + 1;

        Equipment equipment = Equipment.builder()
                .subCategory(subCategory)
                .model(dto.getModel())
                .modelCode(categoryCode + "-" + subCategory.getSubCategoryCode() + "-" + modelSeq)
                .modelSequence(modelSeq)
                .stock(dto.getStock())
                .build();
        equipmentRepository.save(equipment);

        List<FileMeta> savedFiles = new ArrayList<>();

        if(files != null && !files.isEmpty()) {
            int fileOrder = 0;

            String categoryLabel = equipment.getSubCategory().getCategory().getLabel();
            String subCategoryLabel = equipment.getSubCategory().getLabel();
            String typeKey = String.format("equipment_%s_%s", categoryLabel, subCategoryLabel);

            List<String> fileUrls = fileService.saveFiles(files, typeKey);

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
        long baseSequence = equipmentItemRepository.findMaxSequenceByModel(equipment.getModel())
                .orElse(0L); // 기존 max sequence 가져오기

        List<EquipmentItem> items = new ArrayList<>();
        for (int i = 0; i < equipment.getStock(); i++) {
            long sequence = baseSequence + i + 1; // 반복마다 시퀀스 증가
            String serialNumber = generateSerialNumber(equipment.getModelCode(), sequence);

            EquipmentItem item = EquipmentItem.builder()
                    .equipment(equipment)
                    .serialNumber(serialNumber)
                    .sequence(sequence)
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
                .subCategory(equipment.getSubCategory().getLabel())
                .model(equipment.getModel())
                .stock(equipment.getStock())
                .images(lists)
                .createdAt(equipment.getCreatedAt())
                .updatedAt(equipment.getUpdatedAt())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponseDto<EquipmentDto> getEquipment(SearchParamDto paramDto) {
        Pageable pageable = paramDto.getPageable();
        Page<EquipmentDto> equipmentDtosPage  = equipmentRepository.findByFilters(paramDto, pageable);

        return PageResponseDto.<EquipmentDto>builder()
                .content(equipmentDtosPage .getContent())
                .page(equipmentDtosPage .getNumber() + 1)
                .size(equipmentDtosPage .getSize())
                .totalElements(equipmentDtosPage .getTotalElements())
                .totalPages(equipmentDtosPage .getTotalPages())
                .numberOfElements(equipmentDtosPage .getNumberOfElements())
                .first(equipmentDtosPage .isFirst())
                .last(equipmentDtosPage .isLast())
                .empty(equipmentDtosPage .isEmpty())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public EquipmentItemListDto getEquipmentItem(Long equipmentId, SearchParamDto paramDto) {
        Pageable pageable = paramDto.getPageable();
        EquipmentStatus status = paramDto.getEquipmentStatusEnum();

        // 장비 요약 정보는 서비스에서 직접 조회
        Equipment equipment = equipmentRepository.findById(equipmentId)
                .orElseThrow(() -> new CustomException(ErrorType.EQUIPMENT_NOT_FOUND));

        Integer availableStock = equipmentItemRepository.countByEquipment_EquipmentIdAndStatus(equipmentId, EquipmentStatus.AVAILABLE);
        Integer totalStock = equipmentItemRepository.countByEquipment_EquipmentId(equipmentId);

        EquipmentDto equipmentSummary = EquipmentDto.builder()
                .equipmentId(equipment.getEquipmentId())
                .category(equipment.getSubCategory().getCategory().getLabel())
                .subCategory(equipment.getSubCategory().getLabel())
                .model(equipment.getModel())
                .availableStock(availableStock)
                .totalStock(totalStock)
                .imageUrl(fileRepository.findUrlsByEquipmentId(equipmentId).stream().findFirst().orElse(null))
                .build();

        Page<EquipmentItemDto> equipmentItemsDtosPage = equipmentItemRepository.findByStatus(equipmentId, status, pageable);

        return EquipmentItemListDto.builder()
                .equipmentSummary(equipmentSummary)
                .equipmentItems(
                        PageResponseDto.<EquipmentItemDto>builder()
                            .content(equipmentItemsDtosPage.getContent())
                            .page(equipmentItemsDtosPage.getNumber() + 1)
                            .size(equipmentItemsDtosPage.getSize())
                            .totalElements(equipmentItemsDtosPage.getTotalElements())
                            .totalPages(equipmentItemsDtosPage.getTotalPages())
                            .numberOfElements(equipmentItemsDtosPage.getNumberOfElements())
                            .first(equipmentItemsDtosPage.isFirst())
                            .last(equipmentItemsDtosPage.isLast())
                            .empty(equipmentItemsDtosPage.isEmpty())
                    .build()
                )
                .build();
    }

    @Override
    @Transactional
    public void increaseStock(Long equipmentId, IncreaseStockRequestDto dto) {
        Equipment equipment = equipmentRepository.findByEquipmentId(equipmentId)
                .orElseThrow(() -> new CustomException(ErrorType.EQUIPMENT_NOT_FOUND));

        // EquipmentItem 추가 (stock 수량만큼)
        long baseSequence = equipmentItemRepository.findMaxSequenceByModel(equipment.getModel())
                .orElse(0L);
        List<EquipmentItem> items = new ArrayList<>();

        for (int i = 0; i < dto.getAmount(); i++) {
            long sequence = baseSequence + i + 1; // 반복마다 시퀀스 증가
            String serialNumber = generateSerialNumber(equipment.getModelCode(), sequence);

            EquipmentItem item = EquipmentItem.builder()
                    .equipment(equipment)
                    .serialNumber(serialNumber)
                    .sequence(sequence)
                    .status(EquipmentStatus.AVAILABLE) // 초기 상태
                    .build();

            items.add(item);
        }
        equipmentItemRepository.saveAll(items);

        equipment.increaseStock(dto.getAmount());
    }

    @Override
    public void updateEquipmentImage(Long equipmentId, List<MultipartFile> files) {
        Equipment equipment = equipmentRepository.findById(equipmentId)
                .orElseThrow(() -> new CustomException(ErrorType.EQUIPMENT_NOT_FOUND));

        if(files != null && !files.isEmpty()) {
            // 기존 이미지 조회
            List<FileMeta> existingFiles = fileRepository.findByRelatedTypeAndRelatedId("equipment", equipmentId);

            // 1. 기존이 있다면 삭제
            if (!existingFiles.isEmpty()) {
                fileRepository.deleteAll(existingFiles);
                // (선택) 실제 파일 삭제: fileService.deleteFiles(existingFiles);
            }

            // 2. 새 이미지 저장
            int fileOrder = 0;
            List<String> fileUrls = fileService.saveFiles(files, "equipment");
            List<FileMeta> savedFiles = new ArrayList<>();

            for (int i = 0; i < files.size(); i++) {
                MultipartFile file = files.get(i);
                String url = fileUrls.get(i);

                FileMeta meta = FileMeta.builder()
                        .relatedType("equipment")
                        .relatedId(equipment.getEquipmentId())
                        .originalName(file.getOriginalFilename())
                        .uniqueName(url.substring(url.lastIndexOf("/") + 1))
                        .fileOrder(fileOrder++)
                        .fileType(file.getContentType())
                        .filePath(url)
                        .fileSize(file.getSize())
                        .build();
                savedFiles.add(meta);
            }
            fileRepository.saveAll(savedFiles);
        }
    }

    private String generateSerialNumber(String modelCode, long sequence) {
        // 랜덤 4자리 숫자 생성
        int random4 = new Random().nextInt(10000); // 0~9999
        String random4Str = String.format("%04d", random4); // 4자리 고정, 앞에 0 채우기

        return modelCode + "-" + sequence + "-" + random4Str;
    }
}
