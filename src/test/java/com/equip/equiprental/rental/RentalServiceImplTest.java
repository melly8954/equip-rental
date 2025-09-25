package com.equip.equiprental.rental;

import com.equip.equiprental.common.dto.PageResponseDto;
import com.equip.equiprental.common.dto.SearchParamDto;
import com.equip.equiprental.common.exception.CustomException;
import com.equip.equiprental.common.exception.ErrorType;
import com.equip.equiprental.equipment.domain.Equipment;
import com.equip.equiprental.equipment.domain.SubCategory;
import com.equip.equiprental.equipment.repository.EquipmentRepository;
import com.equip.equiprental.equipment.repository.EquipmentItemRepository;
import com.equip.equiprental.equipment.repository.EquipmentItemHistoryRepository;
import com.equip.equiprental.member.domain.Member;
import com.equip.equiprental.member.repository.MemberRepository;
import com.equip.equiprental.rental.domain.Rental;
import com.equip.equiprental.rental.domain.RentalStatus;
import com.equip.equiprental.rental.dto.AdminRentalDto;
import com.equip.equiprental.rental.dto.RentalRequestDto;
import com.equip.equiprental.rental.dto.RentalResponseDto;
import com.equip.equiprental.rental.repository.RentalItemRepository;
import com.equip.equiprental.rental.repository.RentalRepository;
import com.equip.equiprental.rental.service.RentalServiceImpl;
import org.junit.jupiter.api.BeforeEach;
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
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("RentalServiceImpl 단위 테스트")
public class RentalServiceImplTest {

    @Mock private MemberRepository memberRepository;
    @Mock private EquipmentRepository equipmentRepository;
    @Mock private EquipmentItemRepository equipmentItemRepository;
    @Mock private EquipmentItemHistoryRepository equipmentItemHistoryRepository;
    @Mock private RentalRepository rentalRepository;
    @Mock private RentalItemRepository rentalItemRepository;

    @InjectMocks private RentalServiceImpl rentalService;

    @Nested
    @DisplayName("requestRental 메서드 테스트")
    class requestRental {
        private Member member;
        private SubCategory subCategory;
        private Equipment equipment;

        @BeforeEach
        void setUp() {
            member = Member.builder()
                    .memberId(1L)
                    .name("TestUser")
                    .build();

            subCategory = SubCategory.builder()
                    .subCategoryId(100L)
                    .label("노트북")
                    .build();

            equipment = Equipment.builder()
                    .equipmentId(1L)
                    .subCategory(subCategory)
                    .model("MacBook Pro")
                    .modelCode("MBP2023")
                    .modelSequence(1L)
                    .stock(5)
                    .build();

        }

        @Test
        @DisplayName("성공 - 대여 신청 완료")
        void requestRental_success() {
            // given
            RentalRequestDto dto = RentalRequestDto.builder()
                    .equipmentId(equipment.getEquipmentId())
                    .quantity(1)
                    .rentalReason("촬영")
                    .startDate(LocalDate.now().plusDays(1))
                    .endDate(LocalDate.now().plusDays(3))
                    .build();

            when(memberRepository.findByMemberId(member.getMemberId())).thenReturn(Optional.of(member));
            when(equipmentRepository.findById(equipment.getEquipmentId())).thenReturn(Optional.of(equipment));
            when(equipmentItemRepository.countAvailableByEquipmentId(equipment.getEquipmentId())).thenReturn(5);

            // when
            RentalResponseDto response = rentalService.requestRental(dto, member.getMemberId());

            // then
            assertThat(response).isNotNull();
            assertThat(response.getEquipmentId()).isEqualTo(equipment.getEquipmentId());
            assertThat(response.getQuantity()).isEqualTo(dto.getQuantity());
            assertThat(response.getStatus()).isEqualTo(RentalStatus.PENDING);
        }

        @Test
        @DisplayName("예외 - 회원이 존재하지 않음")
        void memberNotFound() {
            RentalRequestDto dto = RentalRequestDto.builder()
                    .equipmentId(1L)
                    .quantity(1)
                    .startDate(LocalDate.now().plusDays(1))
                    .endDate(LocalDate.now().plusDays(2))
                    .build();

            when(memberRepository.findByMemberId(1L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> rentalService.requestRental(dto, 1L))
                    .isInstanceOf(CustomException.class)
                    .hasMessageContaining(ErrorType.USER_NOT_FOUND.getMessage());
        }

        @Test
        @DisplayName("예외 - 장비가 존재하지 않음")
        void equipmentNotFound() {
            RentalRequestDto dto = RentalRequestDto.builder()
                    .equipmentId(999L)
                    .quantity(1)
                    .startDate(LocalDate.now().plusDays(1))
                    .endDate(LocalDate.now().plusDays(2))
                    .build();

            when(memberRepository.findByMemberId(member.getMemberId())).thenReturn(Optional.of(member));
            when(equipmentRepository.findById(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> rentalService.requestRental(dto, member.getMemberId()))
                    .isInstanceOf(CustomException.class)
                    .hasMessageContaining(ErrorType.EQUIPMENT_NOT_FOUND.getMessage());
        }

        @Test
        @DisplayName("예외 - 올바르지 않은 대여 시작일 요청")
        void invalidStartDate() {
            RentalRequestDto dto = RentalRequestDto.builder()
                    .equipmentId(equipment.getEquipmentId())
                    .quantity(1)
                    .startDate(LocalDate.now().minusDays(1))
                    .endDate(LocalDate.now().plusDays(2))
                    .build();

            when(memberRepository.findByMemberId(member.getMemberId())).thenReturn(Optional.of(member));
            when(equipmentRepository.findById(equipment.getEquipmentId())).thenReturn(Optional.of(equipment));

            assertThatThrownBy(() -> rentalService.requestRental(dto, member.getMemberId()))
                    .isInstanceOf(CustomException.class)
                    .hasMessageContaining(ErrorType.RENTAL_START_DATE_INVALID.getMessage());
        }

        @Test
        @DisplayName("예외 - 올바르지 않은 대여 반납 예정일 요청")
        void invalidEndDate() {
            RentalRequestDto dto = RentalRequestDto.builder()
                    .equipmentId(equipment.getEquipmentId())
                    .quantity(1)
                    .startDate(LocalDate.now().plusDays(2))
                    .endDate(LocalDate.now().plusDays(1))
                    .build();

            when(memberRepository.findByMemberId(member.getMemberId())).thenReturn(Optional.of(member));
            when(equipmentRepository.findById(equipment.getEquipmentId())).thenReturn(Optional.of(equipment));

            assertThatThrownBy(() -> rentalService.requestRental(dto, member.getMemberId()))
                    .isInstanceOf(CustomException.class)
                    .hasMessageContaining(ErrorType.RENTAL_END_DATE_INVALID.getMessage());
        }

        @Test
        @DisplayName("예외 - 대여 재고 부족")
        void invalidQuantity() {
            RentalRequestDto dto = RentalRequestDto.builder()
                    .equipmentId(equipment.getEquipmentId())
                    .quantity(10) // 재고는 5
                    .startDate(LocalDate.now().plusDays(1))
                    .endDate(LocalDate.now().plusDays(3))
                    .build();

            when(memberRepository.findByMemberId(member.getMemberId())).thenReturn(Optional.of(member));
            when(equipmentRepository.findById(equipment.getEquipmentId())).thenReturn(Optional.of(equipment));
            when(equipmentItemRepository.countAvailableByEquipmentId(equipment.getEquipmentId())).thenReturn(5);

            assertThatThrownBy(() -> rentalService.requestRental(dto, member.getMemberId()))
                    .isInstanceOf(CustomException.class)
                    .hasMessageContaining(ErrorType.RENTAL_QUANTITY_EXCEEDS_STOCK.getMessage());
        }
    }

    @Nested
    @DisplayName("getAdminRentalList 메서드 테스트")
    class getAdminRentalList {
        SearchParamDto paramDto = SearchParamDto.builder().page(1).size(10).build();
        Pageable pageable = paramDto.getPageable();

        @Test
        @DisplayName("성공 - PageResponseDto 변환")
        void getAdminRentalList_success() {
            // given
            LocalDate start = LocalDate.now().plusDays(1);
            LocalDate end = LocalDate.now().plusDays(3);
            LocalDateTime createdAt = LocalDateTime.now();

            AdminRentalDto dto1 = AdminRentalDto.builder()
                    .rentalId(1L)
                    .equipmentId(101L)
                    .thumbnailUrl("thumb1.png")
                    .quantity(2)
                    .requestStartDate(start)
                    .requestEndDate(end)
                    .rentalReason("촬영")
                    .createdAt(createdAt)
                    .memberId(1L)
                    .name("홍길동")
                    .department("개발팀")
                    .category("전자기기")
                    .subCategory("노트북")
                    .model("MacBook Pro")
                    .build();

            AdminRentalDto dto2 = AdminRentalDto.builder()
                    .rentalId(2L)
                    .equipmentId(102L)
                    .thumbnailUrl("thumb2.png")
                    .quantity(1)
                    .requestStartDate(start)
                    .requestEndDate(end)
                    .rentalReason("회의")
                    .createdAt(createdAt)
                    .memberId(2L)
                    .name("김철수")
                    .department("관리팀")
                    .category("전자기기")
                    .subCategory("카메라")
                    .model("Canon R6")
                    .build();

            Page<AdminRentalDto> stubPage = new PageImpl<>(List.of(dto1, dto2), pageable, 2);
            when(rentalRepository.findAdminRentals(paramDto, pageable)).thenReturn(stubPage);

            // when
            PageResponseDto<AdminRentalDto> response = rentalService.getAdminRentalList(paramDto);

            // then
            assertThat(response).isNotNull();
            assertThat(response.getContent()).hasSize(2);
            assertThat(response.getContent().get(0).getCategory()).isEqualTo("전자기기");
            assertThat(response.getContent().get(0).getSubCategory()).isEqualTo("노트북");
            assertThat(response.getPage()).isEqualTo(1);
            assertThat(response.getTotalElements()).isEqualTo(2);
            assertThat(response.isFirst()).isTrue();
            assertThat(response.isLast()).isTrue();
            assertThat(response.isEmpty()).isFalse();
        }

        @Test
        @DisplayName("성공 - 빈 페이지 반환")
        void getAdminRentalList_emptyPage() {
            Page<AdminRentalDto> emptyPage = new PageImpl<>(List.of(), pageable, 0);
            when(rentalRepository.findAdminRentals(paramDto, pageable)).thenReturn(emptyPage);

            PageResponseDto<AdminRentalDto> response = rentalService.getAdminRentalList(paramDto);

            assertThat(response).isNotNull();
            assertThat(response.getContent()).isEmpty();
            assertThat(response.getPage()).isEqualTo(1);
            assertThat(response.getTotalElements()).isEqualTo(0);
            assertThat(response.isEmpty()).isTrue();
            assertThat(response.isFirst()).isTrue();
            assertThat(response.isLast()).isTrue();
        }
    }
}
