package com.equip.equiprental.service;

import com.equip.equiprental.common.exception.CustomException;
import com.equip.equiprental.common.exception.ErrorType;
import com.equip.equiprental.equipment.domain.Equipment;
import com.equip.equiprental.equipment.domain.EquipmentCategory;
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
}
