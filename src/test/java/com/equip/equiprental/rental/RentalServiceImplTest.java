package com.equip.equiprental.rental;

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

import java.time.LocalDate;
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
}
