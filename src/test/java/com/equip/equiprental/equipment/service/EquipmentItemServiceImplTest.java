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
import com.equip.equiprental.rental.domain.Rental;
import com.equip.equiprental.rental.domain.RentalItem;
import com.equip.equiprental.rental.repository.RentalItemRepository;
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

import java.time.LocalDate;
import java.time.LocalDateTime;
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
    @Mock private RentalItemRepository rentalItemRepository;

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

        @Test
        @DisplayName("예외 - 대여 중인 장비 상태 변경 불가")
        void updateItemStatus_rentedItem() {
            // given
            Member changer = Member.builder()
                    .memberId(1L)
                    .name("Admin")
                    .build();

            UpdateItemStatusDto dto = UpdateItemStatusDto.builder()
                    .equipmentItemId(1L)
                    .newStatus("AVAILABLE") // 변경 시도
                    .build();

            EquipmentItem item = EquipmentItem.builder()
                    .equipmentItemId(1L)
                    .status(EquipmentStatus.RENTED) // 이미 대여 중
                    .build();

            when(equipmentItemRepository.findById(dto.getEquipmentItemId())).thenReturn(Optional.of(item));

            // when & then
            assertThatThrownBy(() -> equipmentItemService.updateItemStatus(dto, changer))
                    .isInstanceOf(CustomException.class)
                    .extracting("errorType")
                    .isEqualTo(ErrorType.CANNOT_MODIFY_WHILE_RENTED);
        }
    }

    @Nested
    @DisplayName("getItemHistory 메서드 테스트")
    class getItemHistory {
        private SearchParamDto createParamDto() {
            return SearchParamDto.builder()
                    .page(1)
                    .size(10)
                    .build();
        }

        private EquipmentItemHistoryDto createHistory(Long equipmentItemId, String oldStatus, String newStatus, String changedBy) {
            return EquipmentItemHistoryDto.builder()
                    .equipmentItemId(equipmentItemId)
                    .oldStatus(oldStatus)
                    .newStatus(newStatus)
                    .changedBy(changedBy)
                    .rentedUserName("user" + equipmentItemId)
                    .rentedUserDept("dept" + equipmentItemId)
                    .rentalStartDate(LocalDate.of(2025, 1, 1))
                    .actualReturnDate(LocalDate.of(2025, 1, 5))
                    .createdAt(LocalDateTime.of(2025, 1, 1, 12, 0))
                    .build();
        }

        @Test
        @DisplayName("성공 - 장비 아이템 히스토리 조회 성공")
        void whenHistoriesExist_thenReturnPagedResult() {
            // given
            Long equipmentItemId = 1L;
            SearchParamDto paramDto = createParamDto();
            Pageable pageable = paramDto.getPageable();

            EquipmentItemHistoryDto history1 = createHistory(1L, "AVAILABLE", "RENTED", "admin1");
            EquipmentItemHistoryDto history2 = createHistory(2L, "RENTED", "RETURNED", "admin2");

            Page<EquipmentItemHistoryDto> mockPage = new PageImpl<>(List.of(history1, history2), pageable, 2);
            when(equipmentItemHistoryRepository.findHistoriesByEquipmentItemId(equipmentItemId, pageable))
                    .thenReturn(mockPage);

            // when
            PageResponseDto<EquipmentItemHistoryDto> result = equipmentItemService.getItemHistory(equipmentItemId, paramDto);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getContent())
                    .extracting(EquipmentItemHistoryDto::getEquipmentItemId,
                            EquipmentItemHistoryDto::getOldStatus,
                            EquipmentItemHistoryDto::getNewStatus,
                            EquipmentItemHistoryDto::getChangedBy)
                    .containsExactly(
                            tuple(1L, "AVAILABLE", "RENTED", "admin1"),
                            tuple(2L, "RENTED", "RETURNED", "admin2")
                    );

            assertThat(result.getPage()).isEqualTo(1);
            assertThat(result.getSize()).isEqualTo(10);
            assertThat(result.getTotalElements()).isEqualTo(2);
            assertThat(result.getTotalPages()).isEqualTo(1);
            assertThat(result.getNumberOfElements()).isEqualTo(2);
            assertThat(result.isFirst()).isTrue();
            assertThat(result.isLast()).isTrue();
            assertThat(result.isEmpty()).isFalse();

            verify(equipmentItemHistoryRepository).findHistoriesByEquipmentItemId(equipmentItemId, pageable);
        }

        @Test
        @DisplayName("성공 - 히스토리 없음 (빈 페이지)")
        void whenNoHistoriesExist_thenReturnEmptyPage() {
            // given
            Long equipmentItemId = 1L;
            SearchParamDto paramDto = createParamDto();
            Pageable pageable = paramDto.getPageable();

            when(equipmentItemHistoryRepository.findHistoriesByEquipmentItemId(equipmentItemId, pageable))
                    .thenReturn(Page.empty(pageable));

            // when
            PageResponseDto<EquipmentItemHistoryDto> result = equipmentItemService.getItemHistory(equipmentItemId, paramDto);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getContent()).isEmpty();
            assertThat(result.isEmpty()).isTrue();

            assertThat(result.getPage()).isEqualTo(1);
            assertThat(result.getSize()).isEqualTo(10);
            assertThat(result.getTotalElements()).isEqualTo(0);
            assertThat(result.getTotalPages()).isEqualTo(0);
            assertThat(result.getNumberOfElements()).isEqualTo(0);
            assertThat(result.isFirst()).isTrue();
            assertThat(result.isLast()).isTrue();

            verify(equipmentItemHistoryRepository).findHistoriesByEquipmentItemId(equipmentItemId, pageable);
        }
    }
}
