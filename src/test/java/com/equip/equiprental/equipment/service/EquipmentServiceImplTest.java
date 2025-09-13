package com.equip.equiprental.equipment.service;

import com.equip.equiprental.common.dto.PageResponseDto;
import com.equip.equiprental.common.dto.SearchParamDto;
import com.equip.equiprental.common.exception.CustomException;
import com.equip.equiprental.common.exception.ErrorType;
import com.equip.equiprental.equipment.domain.*;
import com.equip.equiprental.equipment.dto.*;
import com.equip.equiprental.equipment.repository.EquipmentItemHistoryRepository;
import com.equip.equiprental.equipment.repository.EquipmentItemRepository;
import com.equip.equiprental.equipment.repository.EquipmentRepository;
import com.equip.equiprental.equipment.service.EquipmentServiceImpl;
import com.equip.equiprental.equipment.util.ModelCodeGenerator;
import com.equip.equiprental.filestorage.repository.FileRepository;
import com.equip.equiprental.filestorage.service.FileService;
import com.equip.equiprental.member.domain.Member;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("EquipmentServiceImpl 단위 테스트")
public class EquipmentServiceImplTest {
    @Mock private EquipmentRepository equipmentRepository;
    @Mock private EquipmentItemRepository equipmentItemRepository;
    @Mock private EquipmentItemHistoryRepository equipmentItemHistoryRepository;
    @Mock private ModelCodeGenerator modelCodeGenerator;
    @Mock private FileRepository fileRepository;
    @Mock private FileService fileService;
    
    @Captor
    ArgumentCaptor<List<EquipmentItem>> itemListCaptor;

    @InjectMocks
    private EquipmentServiceImpl equipmentService;

    @Nested
    @DisplayName("register 메서드 테스트")
    class register {
        @Test
        @DisplayName("성공 - 장비 등록")
        void register_success() {
            // given
            EquipmentRegisterRequest request = EquipmentRegisterRequest.builder()
                    .category("ELECTRONICS")
                    .subCategory("monitor")
                    .model("MODEL_Y")
                    .stock(1)
                    .build();

            MultipartFile file = mock(MultipartFile.class);
            when(file.getOriginalFilename()).thenReturn("test.png");
            when(file.getContentType()).thenReturn("image/png");
            when(file.getSize()).thenReturn(123L);

            List<MultipartFile> files = List.of(file);
            when(equipmentRepository.findByModel("MODEL_Y")).thenReturn(Optional.empty());
            when(modelCodeGenerator.generate(request.getCategory(), request.getSubCategory())).thenReturn("MCODE456");

            when(equipmentRepository.save(any()))
                    .thenAnswer(invocation -> {
                        Equipment eq = invocation.getArgument(0);
                        Field idField = Equipment.class.getDeclaredField("equipmentId");
                        idField.setAccessible(true);
                        idField.set(eq, 1L);
                        return eq;
                    });

            when(fileService.saveFiles(files, "equipment"))
                    .thenReturn(List.of("http://url/to/test.png"));

            // when
            EquipmentRegisterResponse response = equipmentService.register(request, files);

            // then
            assertThat(response.getEquipmentId()).isNotNull();
            assertThat(response.getCategory()).isEqualTo("ELECTRONICS");
            assertThat(response.getSubCategory()).isEqualTo("monitor");
            assertThat(response.getModel()).isEqualTo("MODEL_Y");
            assertThat(response.getStock()).isEqualTo(1);


            verify(equipmentItemRepository, times(1)).saveAll(anyList());
            verify(fileService).saveFiles(files, "equipment");
            verify(fileRepository).saveAll(anyList());
        }

        @Test
        @DisplayName("예외 - 이미 등록된 장비")
        void register_existingModel_throwsException() {
            // given
            EquipmentRegisterRequest request = EquipmentRegisterRequest.builder()
                    .model("EXIST_MODEL")
                    .build();

            when(equipmentRepository.findByModel("EXIST_MODEL"))
                    .thenReturn(Optional.of(new Equipment()));

            // when & then
            assertThatThrownBy(() -> equipmentService.register(request, null))
                    .isInstanceOf(CustomException.class)
                    .extracting("errorType")
                    .isEqualTo(ErrorType.EXIST_EQUIPMENT_MODEL_CODE);
        }
    }

    @Nested
    @DisplayName("getEquipment 메서드 테스트")
    class getEquipment {
        @Test
        @DisplayName("성공 - 장비 조회 성공")
        void getEquipment_success() {
            // given
            SearchParamDto paramDto = SearchParamDto.builder()
                    .page(1)
                    .size(10)
                    .category("ELECTRONICS")
                    .build();
            Pageable pageable = paramDto.getPageable();

            EquipmentDto equipment1 = EquipmentDto.builder()
                    .equipmentId(1L)
                    .category("ELECTRONICS")
                    .subCategory("Laptop")
                    .model("LG Gram")
                    .availableStock(5)
                    .totalStock(10)
                    .imageUrl("url1")
                    .build();

            EquipmentDto equipment2 = EquipmentDto.builder()
                    .equipmentId(2L)
                    .category("ELECTRONICS")
                    .subCategory("Monitor")
                    .model("삼성 오디세이")
                    .availableStock(6)
                    .totalStock(11)
                    .imageUrl("url2")
                    .build();

            Page<EquipmentDto> mockPage = new PageImpl<>(List.of(equipment1, equipment2), pageable, 2);
            when(equipmentRepository.findByFilters(paramDto, pageable)).thenReturn(mockPage);

            // when
            PageResponseDto<EquipmentDto> result = equipmentService.getEquipment(paramDto);

            // then
            assertThat(result).isNotNull();

            assertThat(result.getContent()).hasSize(2);
            assertThat(result.getContent().get(0).getEquipmentId()).isEqualTo(1L);
            assertThat(result.getContent().get(0).getImageUrl()).isEqualTo("url1");
            assertThat(result.getContent().get(1).getEquipmentId()).isEqualTo(2L);
            assertThat(result.getContent().get(1).getImageUrl()).isEqualTo("url2");

            assertThat(result.getPage()).isEqualTo(pageable.getPageNumber() + 1);
            assertThat(result.getSize()).isEqualTo(pageable.getPageSize());
            assertThat(result.getTotalElements()).isEqualTo(2);
            assertThat(result.getTotalPages()).isEqualTo(1);
            assertThat(result.getNumberOfElements()).isEqualTo(2);

            assertThat(result.isFirst()).isTrue();
            assertThat(result.isLast()).isTrue();
            assertThat(result.isEmpty()).isFalse();
        }

        @Test
        @DisplayName("성공 - 장비 조회 결과 없음")
        void getEquipment_empty() {
            // given
            SearchParamDto paramDto = SearchParamDto.builder()
                    .page(1)
                    .size(10)
                    .category("ELECTRONICS")
                    .build();
            Pageable pageable = paramDto.getPageable();

            Page<EquipmentDto> emptyPage = Page.empty(pageable);
            when(equipmentRepository.findByFilters(paramDto, pageable)).thenReturn(emptyPage);

            // when
            PageResponseDto<EquipmentDto> result = equipmentService.getEquipment(paramDto);

            // then
            assertThat(result).isNotNull();

            assertThat(result.getContent()).isEmpty();
            assertThat(result.isEmpty()).isTrue();

            assertThat(result.getPage()).isEqualTo(pageable.getPageNumber() + 1);
            assertThat(result.getSize()).isEqualTo(pageable.getPageSize());
            assertThat(result.getTotalElements()).isEqualTo(0);
            assertThat(result.getTotalPages()).isEqualTo(0);
            assertThat(result.getNumberOfElements()).isEqualTo(0);

            assertThat(result.isFirst()).isTrue();
            assertThat(result.isLast()).isTrue();
        }
    }

    @Nested
    @DisplayName("getEquipmentItem 메서드 테스트")
    class getEquipmentItem {
        @Test
        @DisplayName("성공 - 장비와 아이템 조회 성공")
        void getEquipmentItem_success() {
            // given
            Long equipmentId = 1L;
            SearchParamDto paramDto = SearchParamDto.builder()
                    .page(1)
                    .size(10)
                    .equipmentStatus("AVAILABLE")
                    .build();

            Pageable pageable = paramDto.getPageable();

            Equipment equipment = Equipment.builder()
                    .equipmentId(equipmentId)
                    .category(EquipmentCategory.ELECTRONICS)
                    .subCategory("Laptop")
                    .model("LG Gram")
                    .build();

            when(equipmentRepository.findById(equipmentId)).thenReturn(Optional.of(equipment));
            when(equipmentItemRepository.countByEquipment_EquipmentIdAndStatus(equipmentId, EquipmentStatus.AVAILABLE))
                    .thenReturn(5);
            when(equipmentItemRepository.countByEquipment_EquipmentId(equipmentId))
                    .thenReturn(10);
            when(fileRepository.findUrlsByEquipmentId(equipmentId))
                    .thenReturn(List.of("url1"));

            EquipmentItemDto item1 = EquipmentItemDto.builder().equipmentItemId(1L).status(EquipmentStatus.AVAILABLE).build();
            EquipmentItemDto item2 = EquipmentItemDto.builder().equipmentItemId(2L).status(EquipmentStatus.AVAILABLE).build();
            Page<EquipmentItemDto> itemsPage = new PageImpl<>(List.of(item1, item2), pageable, 2);

            when(equipmentItemRepository.findByStatus(equipmentId, EquipmentStatus.AVAILABLE, pageable))
                    .thenReturn(itemsPage);

            // when
            EquipmentItemListDto result = equipmentService.getEquipmentItem(equipmentId, paramDto);

            // then
            // 장비 요약 검증
            assertThat(result.getEquipmentSummary().getEquipmentId()).isEqualTo(equipmentId);
            assertThat(result.getEquipmentSummary().getAvailableStock()).isEqualTo(5);
            assertThat(result.getEquipmentSummary().getTotalStock()).isEqualTo(10);
            assertThat(result.getEquipmentSummary().getImageUrl()).isEqualTo("url1");

            // 장비 아이템 페이지 검증
            assertThat(result.getEquipmentItems().getContent()).hasSize(2)
                    .extracting(EquipmentItemDto::getEquipmentItemId)
                    .containsExactly(1L, 2L);

            assertThat(result.getEquipmentItems()).extracting(
                    PageResponseDto::getPage,
                    PageResponseDto::getSize,
                    PageResponseDto::getTotalElements,
                    PageResponseDto::getTotalPages,
                    PageResponseDto::getNumberOfElements,
                    PageResponseDto::isFirst,
                    PageResponseDto::isLast,
                    PageResponseDto::isEmpty
            ).containsExactly(1, 10, 2L, 1, 2, true, true, false);
        }

        @Test
        @DisplayName("성공 - 장비는 존재하지만 아이템 없음 (빈 페이지)")
        void getEquipmentItem_emptyItems() {
            // given
            Long equipmentId = 1L;
            SearchParamDto paramDto = SearchParamDto.builder()
                    .page(1)
                    .size(10)
                    .equipmentStatus("AVAILABLE")
                    .build();

            Pageable pageable = paramDto.getPageable();

            Equipment equipment = Equipment.builder()
                    .equipmentId(equipmentId)
                    .category(EquipmentCategory.ELECTRONICS)
                    .subCategory("Laptop")
                    .model("LG Gram")
                    .build();

            when(equipmentRepository.findById(equipmentId)).thenReturn(Optional.of(equipment));
            when(equipmentItemRepository.countByEquipment_EquipmentIdAndStatus(equipmentId, EquipmentStatus.AVAILABLE))
                    .thenReturn(0); // 아이템 없음
            when(equipmentItemRepository.countByEquipment_EquipmentId(equipmentId))
                    .thenReturn(0); // 전체 재고도 0
            when(fileRepository.findUrlsByEquipmentId(equipmentId))
                    .thenReturn(List.of("url1"));

            Page<EquipmentItemDto> emptyPage = Page.empty(pageable);
            when(equipmentItemRepository.findByStatus(equipmentId, EquipmentStatus.AVAILABLE, pageable))
                    .thenReturn(emptyPage);

            // when
            EquipmentItemListDto result = equipmentService.getEquipmentItem(equipmentId, paramDto);

            // then
            // 장비 요약 검증
            assertThat(result.getEquipmentSummary().getEquipmentId()).isEqualTo(equipmentId);
            assertThat(result.getEquipmentSummary().getAvailableStock()).isEqualTo(0);
            assertThat(result.getEquipmentSummary().getTotalStock()).isEqualTo(0);
            assertThat(result.getEquipmentSummary().getImageUrl()).isEqualTo("url1");

            // 장비 아이템 페이지 검증 (빈 페이지)
            assertThat(result.getEquipmentItems().getContent()).isEmpty();
            assertThat(result.getEquipmentItems().isEmpty()).isTrue();

            assertThat(result.getEquipmentItems()).extracting(
                    PageResponseDto::getPage,
                    PageResponseDto::getSize,
                    PageResponseDto::getTotalElements,
                    PageResponseDto::getTotalPages,
                    PageResponseDto::getNumberOfElements,
                    PageResponseDto::isFirst,
                    PageResponseDto::isLast
            ).containsExactly(1, 10, 0L, 0, 0, true, true);
        }

        @Test
        @DisplayName("예외 - 장비가 존재하지 않음")
        void getEquipmentItem_notFound() {
            Long equipmentId = 1L;
            SearchParamDto paramDto = SearchParamDto.builder().build();

            // Mock: 장비가 존재하지 않음
            when(equipmentRepository.findById(equipmentId)).thenReturn(Optional.empty());

            // 검증: CustomException 발생
            assertThatThrownBy(() -> equipmentService.getEquipmentItem(equipmentId, paramDto))
                    .isInstanceOf(CustomException.class)
                    .extracting("errorType")
                    .isEqualTo(ErrorType.EQUIPMENT_NOT_FOUND);
        }
    }

    @Nested
    @DisplayName("increaseStock 메서드 테스트")
    class increaseStock {
        @Test
        @DisplayName("성공 - 재고 증가 및 아이템 추가")
        void increaseStock_success() {
            // given
            Long equipmentId = 1L;
            IncreaseStockRequestDto dto = IncreaseStockRequestDto.builder()
                    .amount(3)
                    .build();

            Equipment equipment = Equipment.builder()
                    .equipmentId(equipmentId)
                    .model("LG Gram")
                    .modelCode("LG001")
                    .stock(5) // 기존 재고
                    .build();

            when(equipmentRepository.findByEquipmentId(equipmentId)).thenReturn(Optional.of(equipment));
            when(equipmentItemRepository.findMaxSequenceByModel(equipment.getModel())).thenReturn(Optional.of(10L));

            // when
            equipmentService.increaseStock(equipmentId, dto);

            // then
            // saveAll 호출 검증
            verify(equipmentItemRepository).saveAll(itemListCaptor.capture());

            List<EquipmentItem> savedItems = itemListCaptor.getValue();
            assertThat(savedItems).hasSize(3);

            // 시퀀스와 serialNumber 검증
            assertThat(savedItems.get(0).getSequence()).isEqualTo(11L);
            assertThat(savedItems.get(1).getSequence()).isEqualTo(12L);
            assertThat(savedItems.get(2).getSequence()).isEqualTo(13L);

            assertThat(savedItems.get(0).getSerialNumber()).startsWith("LG001");
            assertThat(savedItems.get(1).getSerialNumber()).startsWith("LG001");
            assertThat(savedItems.get(2).getSerialNumber()).startsWith("LG001");

            // 상태 검증
            assertThat(savedItems).allMatch(item -> item.getStatus() == EquipmentStatus.AVAILABLE);

            // Equipment 재고 증가 검증
            assertThat(equipment.getStock()).isEqualTo(8); // 기존 5 + 3
        }

        @Test
        @DisplayName("예외 - 장비가 존재하지 않음")
        void increaseStock_equipmentNotFound() {
            Long equipmentId = 1L;
            IncreaseStockRequestDto dto = IncreaseStockRequestDto.builder()
                    .amount(3)
                    .build();

            when(equipmentRepository.findByEquipmentId(equipmentId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> equipmentService.increaseStock(equipmentId, dto))
                    .isInstanceOf(CustomException.class)
                    .extracting("errorType")
                    .isEqualTo(ErrorType.EQUIPMENT_NOT_FOUND);
        }
    }

    @Nested
    @DisplayName("updateItemStatus 메서드 테스트")
    class updateItemStatus {
        @Test
        @DisplayName("성공 - 장비 아이템 상태 변경 및 히스토리 저장")
        void updateItemStatus_success() {
            // given
            Member changer = Member.builder()
                    .memberId(1L)
                    .name("Admin")
                    .build();
            UpdateItemStatusDto dto = UpdateItemStatusDto.builder()
                    .equipmentItemId(1L)
                    .newStatus("RENTED")
                    .build();

            EquipmentItem item = EquipmentItem.builder()
                    .equipmentItemId(1L)
                    .status(EquipmentStatus.AVAILABLE)
                    .build();

            when(equipmentItemRepository.findById(dto.getEquipmentItemId())).thenReturn(Optional.of(item));

            // when
            equipmentService.updateItemStatus(dto, changer);

            // then
            // 상태 변경 검증
            assertThat(item.getStatus()).isEqualTo(EquipmentStatus.RENTED);

            // 히스토리 저장 검증
            ArgumentCaptor<EquipmentItemHistory> captor = ArgumentCaptor.forClass(EquipmentItemHistory.class);
            verify(equipmentItemHistoryRepository).save(captor.capture());

            EquipmentItemHistory savedHistory = captor.getValue();
            assertThat(savedHistory.getItem()).isEqualTo(item);
            assertThat(savedHistory.getOldStatus()).isEqualTo(EquipmentStatus.AVAILABLE);
            assertThat(savedHistory.getNewStatus()).isEqualTo(EquipmentStatus.RENTED);
            assertThat(savedHistory.getChangedBy()).isEqualTo(changer);
        }

        @Test
        @DisplayName("예외 - 장비 아이템 존재하지 않음")
        void updateItemStatus_itemNotFound() {
            UpdateItemStatusDto dto = UpdateItemStatusDto.builder()
                    .equipmentItemId(1L)
                    .newStatus("RENTED")
                    .build();

            when(equipmentItemRepository.findById(dto.getEquipmentItemId())).thenReturn(Optional.empty());

            assertThatThrownBy(() -> equipmentService.updateItemStatus(dto, null))
                    .isInstanceOf(CustomException.class)
                    .extracting("errorType")
                    .isEqualTo(ErrorType.EQUIPMENT_ITEM_NOT_FOUND);
        }
    }

    @Nested
    @DisplayName("getItemHistory 메서드 테스트")
    class getItemHistory {
        @Test
        @DisplayName("성공 - 장비 아이템 히스토리 조회")
        void getItemHistory_success() {
            // given
            Long equipmentItemId = 1L;
            SearchParamDto paramDto = SearchParamDto.builder()
                    .page(1)
                    .size(10)
                    .build();
            Pageable pageable = paramDto.getPageable();

            EquipmentItemHistoryDto history1 = EquipmentItemHistoryDto.builder()
                    .oldStatus("AVAILABLE")
                    .newStatus("RENTED")
                    .changedBy("Admin")
                    .build();

            EquipmentItemHistoryDto history2 = EquipmentItemHistoryDto.builder()
                    .oldStatus("AVAILABLE")
                    .newStatus("RENTED")
                    .changedBy("Admin")
                    .build();

            // Page 생성
            Page<EquipmentItemHistoryDto> mockPage = new PageImpl<>(List.of(history1, history2), pageable, 2);

            // Mockito stub
            when(equipmentItemHistoryRepository.findHistoriesByEquipmentItemId(equipmentItemId, pageable))
                    .thenReturn(mockPage);

            // when
            PageResponseDto<EquipmentItemHistoryDto> result = equipmentService.getItemHistory(equipmentItemId, paramDto);

            // then
            assertThat(result.getContent()).hasSize(2)
                    .extracting(EquipmentItemHistoryDto::getOldStatus, EquipmentItemHistoryDto::getNewStatus, EquipmentItemHistoryDto::getChangedBy)
                    .containsExactly(
                            tuple("AVAILABLE", "RENTED", "Admin"),
                            tuple("AVAILABLE", "RENTED", "Admin")
                    );

            assertThat(result.getPage()).isEqualTo(pageable.getPageNumber() + 1);
            assertThat(result.getSize()).isEqualTo(pageable.getPageSize());
            assertThat(result.getTotalElements()).isEqualTo(2);
            assertThat(result.getTotalPages()).isEqualTo(1);
            assertThat(result.getNumberOfElements()).isEqualTo(2);

            assertThat(result.isFirst()).isTrue();
            assertThat(result.isLast()).isTrue();
            assertThat(result.isEmpty()).isFalse();
        }


        @Test
        @DisplayName("성공 - 히스토리 없음 (빈 페이지)")
        void getItemHistory_empty() {
            // given
            Long equipmentItemId = 1L;
            SearchParamDto paramDto = SearchParamDto.builder()
                    .page(1)
                    .size(10)
                    .build();
            Pageable pageable = paramDto.getPageable();

            Page<EquipmentItemHistoryDto> emptyPage = Page.empty(pageable);
            when(equipmentItemHistoryRepository.findHistoriesByEquipmentItemId(equipmentItemId, pageable))
                    .thenReturn(emptyPage);

            // when
            PageResponseDto<EquipmentItemHistoryDto> result = equipmentService.getItemHistory(equipmentItemId, paramDto);

            // then
            assertThat(result.getContent()).isEmpty();
            assertThat(result.isEmpty()).isTrue();

            assertThat(result.getPage()).isEqualTo(pageable.getPageNumber() + 1);
            assertThat(result.getSize()).isEqualTo(pageable.getPageSize());
            assertThat(result.getTotalElements()).isEqualTo(0);
            assertThat(result.getTotalPages()).isEqualTo(0);
            assertThat(result.getNumberOfElements()).isEqualTo(0);

            assertThat(result.isFirst()).isTrue();
            assertThat(result.isLast()).isTrue();
        }
    }
}
