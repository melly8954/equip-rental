package com.equip.equiprental.equipment.service;

import com.equip.equiprental.common.dto.PageResponseDto;
import com.equip.equiprental.common.dto.SearchParamDto;
import com.equip.equiprental.common.exception.CustomException;
import com.equip.equiprental.common.exception.ErrorType;
import com.equip.equiprental.equipment.domain.EquipmentItem;
import com.equip.equiprental.equipment.domain.EquipmentItemHistory;
import com.equip.equiprental.equipment.domain.EquipmentStatus;
import com.equip.equiprental.equipment.dto.EquipmentItemHistoryDto;
import com.equip.equiprental.equipment.dto.UpdateItemStatusDto;
import com.equip.equiprental.equipment.repository.EquipmentItemHistoryRepository;
import com.equip.equiprental.equipment.repository.EquipmentItemRepository;
import com.equip.equiprental.member.domain.Member;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("EquipmentItemServiceImpl 단위 테스트")
public class EquipmentItemServiceImplTest {
    @Mock private EquipmentItemRepository equipmentItemRepository;
    @Mock private EquipmentItemHistoryRepository equipmentItemHistoryRepository;


    @InjectMocks
    private EquipmentItemServiceImpl equipmentItemService;

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
            equipmentItemService.updateItemStatus(dto, changer);

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

            assertThatThrownBy(() -> equipmentItemService.updateItemStatus(dto, null))
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
            PageResponseDto<EquipmentItemHistoryDto> result = equipmentItemService.getItemHistory(equipmentItemId, paramDto);

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
            PageResponseDto<EquipmentItemHistoryDto> result = equipmentItemService.getItemHistory(equipmentItemId, paramDto);

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
