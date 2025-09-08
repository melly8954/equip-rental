package com.equip.equiprental.service;

import com.equip.equiprental.common.dto.PageResponseDto;
import com.equip.equiprental.common.dto.SearchParamDto;
import com.equip.equiprental.common.exception.CustomException;
import com.equip.equiprental.common.exception.ErrorType;
import com.equip.equiprental.equipment.domain.Equipment;
import com.equip.equiprental.equipment.domain.EquipmentCategory;
import com.equip.equiprental.equipment.dto.EquipmentDto;
import com.equip.equiprental.equipment.dto.EquipmentRegisterRequest;
import com.equip.equiprental.equipment.dto.EquipmentRegisterResponse;
import com.equip.equiprental.equipment.repository.EquipmentItemRepository;
import com.equip.equiprental.equipment.repository.EquipmentRepository;
import com.equip.equiprental.equipment.service.EquipmentServiceImpl;
import com.equip.equiprental.equipment.util.ModelCodeGenerator;
import com.equip.equiprental.filestorage.repository.FileRepository;
import com.equip.equiprental.filestorage.service.FileService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("EquipmentServiceImpl 단위 테스트")
public class EquipmentServiceImplTest {
    @Mock private EquipmentRepository equipmentRepository;
    @Mock private EquipmentItemRepository equipmentItemRepository;
    @Mock private ModelCodeGenerator modelCodeGenerator;
    @Mock private FileRepository fileRepository;
    @Mock private FileService fileService;

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
            when(modelCodeGenerator.generate()).thenReturn("MCODE456");
            when(equipmentRepository.countByModel("MODEL_Y")).thenReturn(0L);

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
                    .category("ELECTRONICS") // 문자열 -> Enum 변환
                    .build();

            Pageable pageable = paramDto.getPageable();

            Equipment equipment1 = Equipment.builder()
                    .equipmentId(1L)
                    .category(EquipmentCategory.ELECTRONICS)
                    .subCategory("Laptop")
                    .model("LG Gram")
                    .stock(5)
                    .build();

            Equipment equipment2 = Equipment.builder()
                    .equipmentId(2L)
                    .category(EquipmentCategory.ELECTRONICS)
                    .subCategory("Monitor")
                    .model("삼성 오디세이")
                    .stock(3)
                    .build();

            Page<Equipment> mockPage = new PageImpl<>(List.of(equipment1, equipment2), pageable, 2);

            when(equipmentRepository.findByFilters(
                    eq(EquipmentCategory.ELECTRONICS),
                    isNull(),
                    isNull(),
                    eq(pageable))
            ).thenReturn(mockPage);

            when(fileRepository.findUrlsByEquipmentId(1L)).thenReturn(List.of("url1"));
            when(fileRepository.findUrlsByEquipmentId(2L)).thenReturn(List.of("url2"));

            // when
            PageResponseDto<EquipmentDto> result = equipmentService.getEquipment(paramDto);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(2);

            EquipmentDto dto1 = result.getContent().get(0);
            assertThat(dto1.getEquipmentId()).isEqualTo(1L);
            assertThat(dto1.getCategory()).isEqualTo("ELECTRONICS");
            assertThat(dto1.getSubCategory()).isEqualTo("Laptop");
            assertThat(dto1.getModel()).isEqualTo("LG Gram");
            assertThat(dto1.getStock()).isEqualTo(5);
            assertThat(dto1.getImageUrl()).isEqualTo("url1");

            EquipmentDto dto2 = result.getContent().get(1);
            assertThat(dto2.getEquipmentId()).isEqualTo(2L);
            assertThat(dto2.getModel()).isEqualTo("삼성 오디세이");
            assertThat(dto2.getImageUrl()).isEqualTo("url2");
        }

        @Test
        @DisplayName("성공 - 장비 조회 성공 (결과 없음,empty)")
        void getEquipment_empty() {
            // given
            SearchParamDto paramDto = SearchParamDto.builder()
                    .page(1)
                    .size(10)
                    .category("ELECTRONICS")
                    .build();

            Pageable pageable = paramDto.getPageable();
            Page<Equipment> emptyPage = Page.empty(pageable);

            when(equipmentRepository.findByFilters(
                    eq(EquipmentCategory.ELECTRONICS),
                    isNull(),
                    isNull(),
                    eq(pageable))
            ).thenReturn(emptyPage);

            // when
            PageResponseDto<EquipmentDto> result = equipmentService.getEquipment(paramDto);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getContent()).isEmpty();
            assertThat(result.isEmpty()).isTrue();
            assertThat(result.getTotalElements()).isEqualTo(0);
        }

        @Test
        @DisplayName("예외 - 잘못된 카테고리 요청")
        void getEquipment_invalidCategory_throwsException() {
            // given
            SearchParamDto paramDto = SearchParamDto.builder()
                    .page(1)
                    .size(10)
                    .category("INVALID") // 존재하지 않는 카테고리
                    .build();

            // when & then
            assertThatThrownBy(() -> equipmentService.getEquipment(paramDto))
                    .isInstanceOf(CustomException.class)
                    .extracting("errorType")
                    .isEqualTo(ErrorType.INVALID_CATEGORY_REQUEST);

            verifyNoInteractions(equipmentRepository); // Repository 호출 안 됨
        }
    }
}
