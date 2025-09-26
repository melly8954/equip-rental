package com.equip.equiprental.rental;


import com.equip.equiprental.common.dto.PageResponseDto;
import com.equip.equiprental.common.dto.SearchParamDto;
import com.equip.equiprental.common.exception.CustomException;
import com.equip.equiprental.common.exception.ErrorType;
import com.equip.equiprental.equipment.repository.EquipmentItemHistoryRepository;
import com.equip.equiprental.member.repository.MemberRepository;
import com.equip.equiprental.rental.domain.RentalItem;
import com.equip.equiprental.rental.domain.RentalItemStatus;
import com.equip.equiprental.rental.dto.AdminRentalDto;
import com.equip.equiprental.rental.dto.AdminRentalItemDto;
import com.equip.equiprental.rental.dto.ExtendRentalItemDto;
import com.equip.equiprental.rental.repository.RentalItemRepository;
import com.equip.equiprental.rental.service.RentalItemServiceImpl;
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

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("RentalItemServiceImpl 단위 테스트")
public class RentalItemServiceImplTest {
    @Mock private RentalItemRepository rentalItemRepository;
    @Mock private EquipmentItemHistoryRepository equipmentItemHistoryRepository;
    @Mock private MemberRepository memberRepository;

    @InjectMocks
    private RentalItemServiceImpl rentalItemService;

    @Nested
    @DisplayName("getAdminRentalItemLists 메서드 테스트")
    class getAdminRentalItemLists {
        SearchParamDto paramDto = SearchParamDto.builder().page(1).size(10).build();
        Pageable pageable = paramDto.getPageable();

        @Test
        @DisplayName("성공 - PageResponseDto 변환")
        void getAdminRentalItemList_success() {
            AdminRentalItemDto dto1 = AdminRentalItemDto.builder()
                    .rentalItemId(1L)
                    .rentalId(101L)
                    .thumbnailUrl("https://example.com/image.jpg")
                    .category("카테고리1")
                    .subCategory("서브카테고리A")
                    .model("Model-X")
                    .serialName("SN-12345")
                    .memberName("홍길동")
                    .department("개발팀")
                    .startDate(LocalDate.now().plusDays(1))
                    .endDate(LocalDate.now().plusDays(3))
                    .actualReturnDate(LocalDate.now().plusDays(2))
                    .status(RentalItemStatus.RENTED)  // 예시: RENTED, RETURNED 등
                    .isExtended(false)
                    .build();

            AdminRentalItemDto dto2 = AdminRentalItemDto.builder()
                    .rentalItemId(2L)
                    .rentalId(102L)
                    .thumbnailUrl("https://example.com/image.jpg")
                    .category("카테고리2")
                    .subCategory("서브카테고리B")
                    .model("Model-Y")
                    .serialName("SB-12345")
                    .memberName("김철수")
                    .department("개발팀")
                    .startDate(LocalDate.now().plusDays(1))
                    .endDate(LocalDate.now().plusDays(3))
                    .actualReturnDate(LocalDate.now().plusDays(2))
                    .status(RentalItemStatus.RENTED)
                    .isExtended(false)
                    .build();

            Page<AdminRentalItemDto> stubPage = new PageImpl<>(List.of(dto1, dto2), pageable, 2);
            when(rentalItemRepository.findAdminRentalItems(paramDto, pageable)).thenReturn(stubPage);

            // when
            PageResponseDto<AdminRentalItemDto> response = rentalItemService.getAdminRentalItemLists(paramDto);

            // then
            assertThat(response).isNotNull();
            assertThat(response.getContent()).hasSize(2);
            assertThat(response.getPage()).isEqualTo(1);
            assertThat(response.getTotalElements()).isEqualTo(2);
            assertThat(response.getTotalPages()).isEqualTo(1);
            assertThat(response.isFirst()).isTrue();
            assertThat(response.isLast()).isTrue();
            assertThat(response.isEmpty()).isFalse();
        }

        @Test
        @DisplayName("성공 - 빈 페이지 반환")
        void getAdminRentalItemList_emptyPage() {
            Page<AdminRentalItemDto> emptyPage = new PageImpl<>(List.of(), pageable, 0);
            when(rentalItemRepository.findAdminRentalItems(paramDto, pageable)).thenReturn(emptyPage);

            PageResponseDto<AdminRentalItemDto> response = rentalItemService.getAdminRentalItemLists(paramDto);

            assertThat(response).isNotNull();
            assertThat(response.getContent()).isEmpty();
            assertThat(response.getPage()).isEqualTo(1);
            assertThat(response.getTotalElements()).isEqualTo(0);
            assertThat(response.getTotalPages()).isEqualTo(0);
            assertThat(response.isEmpty()).isTrue();
            assertThat(response.isFirst()).isTrue();
            assertThat(response.isLast()).isTrue();
        }
    }

    @Nested
    @DisplayName("extendRentalItem 메서드 테스트")
    class extendRentalItem {
        Long rentalItemId = 1L;
        ExtendRentalItemDto dto = new ExtendRentalItemDto(5);

        @Test
        @DisplayName("성공 - 대여 연장")
        void extendRentalItem_success() {
            // given
            RentalItem item = RentalItem.builder()
                    .rentalItemId(rentalItemId)
                    .startDate(LocalDate.now().plusDays(1))
                    .endDate(LocalDate.now().plusDays(3))
                    .status(RentalItemStatus.RENTED)
                    .isExtended(false)
                    .build();

            when(rentalItemRepository.findById(rentalItemId)).thenReturn(Optional.of(item));

            // when
            rentalItemService.extendRentalItem(rentalItemId, dto);

            // then
            assertThat(item.getEndDate()).isEqualTo(LocalDate.now().plusDays(8));
            assertThat(item.getIsExtended()).isTrue();
        }

        @Test
        @DisplayName("실패 - 대여 아이템 없음")
        void extendRentalItem_notFound() {
            // given
            when(rentalItemRepository.findById(rentalItemId)).thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> rentalItemService.extendRentalItem(rentalItemId, dto))
                    .isInstanceOf(CustomException.class)
                    .extracting("errorType")
                    .isEqualTo(ErrorType.RENTAL_NOT_FOUND);
        }
    }
}
