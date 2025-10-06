package com.equip.equiprental.rental;

import com.equip.equiprental.common.dto.PageResponseDto;
import com.equip.equiprental.common.dto.SearchParamDto;
import com.equip.equiprental.common.exception.CustomException;
import com.equip.equiprental.common.exception.ErrorType;
import com.equip.equiprental.equipment.domain.*;
import com.equip.equiprental.equipment.repository.EquipmentRepository;
import com.equip.equiprental.equipment.repository.EquipmentItemRepository;
import com.equip.equiprental.equipment.repository.EquipmentItemHistoryRepository;
import com.equip.equiprental.member.domain.Member;
import com.equip.equiprental.member.repository.MemberRepository;
import com.equip.equiprental.notification.domain.NotificationType;
import com.equip.equiprental.notification.service.iface.NotificationService;
import com.equip.equiprental.rental.domain.Rental;
import com.equip.equiprental.rental.domain.RentalItemStatus;
import com.equip.equiprental.rental.domain.RentalStatus;
import com.equip.equiprental.rental.dto.*;
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
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("RentalServiceImpl 단위 테스트")
public class RentalServiceImplTest {

    @Mock private MemberRepository memberRepository;
    @Mock private EquipmentRepository equipmentRepository;
    @Mock private EquipmentItemRepository equipmentItemRepository;
    @Mock private EquipmentItemHistoryRepository equipmentItemHistoryRepository;
    @Mock private RentalRepository rentalRepository;
    @Mock private RentalItemRepository rentalItemRepository;
    @Mock private NotificationService notificationService;

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
        @DisplayName("성공 - 대여 신청 후 관리자/매니저 알림 발생")
        void requestRental_successWithNotification() {
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

            // 알림 발생 확인
            verify(notificationService).notifyManagersAndAdmins(
                    eq(equipment.getSubCategory().getCategory()),
                    eq(NotificationType.RENTAL_REQUEST),
                    contains("대여 신청"),
                    isNull()
            );
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
                    .extracting("errorType")
                    .isEqualTo(ErrorType.USER_NOT_FOUND);
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
                    .extracting("errorType")
                    .isEqualTo(ErrorType.EQUIPMENT_NOT_FOUND);
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
                    .extracting("errorType")
                    .isEqualTo(ErrorType.RENTAL_START_DATE_INVALID);
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
                    .extracting("errorType")
                    .isEqualTo(ErrorType.RENTAL_END_DATE_INVALID);
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
                    .extracting("errorType")
                    .isEqualTo(ErrorType.RENTAL_QUANTITY_EXCEEDS_STOCK);
        }
    }

    @Nested
    @DisplayName("getAdminRentalList 메서드 테스트")
    class getAdminRentalList {
        RentalFilter paramDto = RentalFilter.builder()
                .page(1)
                .size(10)
                .build();
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
            assertThat(response.getPage()).isEqualTo(1);
            assertThat(response.getTotalElements()).isEqualTo(2);
            assertThat(response.getTotalPages()).isEqualTo(1);
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
            assertThat(response.getTotalPages()).isEqualTo(0);
            assertThat(response.isEmpty()).isTrue();
            assertThat(response.isFirst()).isTrue();
            assertThat(response.isLast()).isTrue();
        }
    }

    @Nested
    @DisplayName("getUserRentalList 메서드 테스트")
    class getUserRentalList {
        RentalFilter paramDto = RentalFilter.builder()
                .page(1)
                .size(10)
                .build();
        Pageable pageable = paramDto.getPageable();
        Long memberId = 1L;

        @Test
        @DisplayName("성공 - PageResponseDto 변환")
        void getUserRentalList_success() {
            // given
            UserRentalDto dto1 = UserRentalDto.builder()
                    .rentalId(1L)
                    .equipmentId(101L)
                    .model("맥북프로")
                    .category("전자기기")
                    .subCategory("노트북")
                    .thumbnailUrl("/images/macbook.jpg")
                    .requestStartDate(LocalDate.of(2025, 1, 1))
                    .requestEndDate(LocalDate.of(2025, 1, 10))
                    .quantity(2)
                    .status("WAITING")
                    .rejectReason(null)
                    .build();

            UserRentalDto dto2 = UserRentalDto.builder()
                    .rentalId(2L)
                    .equipmentId(102L)
                    .model("갤럭시탭")
                    .category("전자기기")
                    .subCategory("태블릿")
                    .thumbnailUrl("/images/galaxy.jpg")
                    .requestStartDate(LocalDate.of(2025, 2, 1))
                    .requestEndDate(LocalDate.of(2025, 2, 5))
                    .quantity(1)
                    .status("APPROVED")
                    .rejectReason(null)
                    .build();

            Page<UserRentalDto> stubPage = new PageImpl<>(List.of(dto1, dto2), pageable, 2);
            when(rentalRepository.findUserRentals(paramDto, pageable, memberId)).thenReturn(stubPage);

            // when
            PageResponseDto<UserRentalDto> response = rentalService.getUserRentalList(paramDto, memberId);

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
        void getUserRentalList_emptyPage() {
            // given
            Page<UserRentalDto> emptyPage = new PageImpl<>(List.of(), pageable, 0);
            when(rentalRepository.findUserRentals(paramDto, pageable, memberId)).thenReturn(emptyPage);

            // when
            PageResponseDto<UserRentalDto> response = rentalService.getUserRentalList(paramDto, memberId);

            // then
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
    @DisplayName("updateRentalStatus 메서드 테스트")
    class updateRentalStatus {
        private Member member;
        private Rental rental;
        private Equipment equipment;
        private List<EquipmentItem> equipmentItems;

        @BeforeEach
        void setUp() {
            member = Member.builder()
                    .memberId(1L)
                    .name("TestUser")
                    .build();
            Category category = Category.builder()
                    .categoryCode("EL")
                    .label("ELECTRONICS")
                    .build();

            SubCategory subCategory = SubCategory.builder()
                    .subCategoryId(1L)
                    .subCategoryCode("MON")
                    .label("Monitor")
                    .category(category)
                    .build();

            equipment = Equipment.builder()
                    .equipmentId(1L)
                    .model("MODEL_X")
                    .stock(5)
                    .subCategory(subCategory) // 여기가 핵심
                    .build();

            rental = Rental.builder()
                    .rentalId(1L)
                    .member(member)
                    .equipment(equipment)
                    .requestStartDate(LocalDate.now().plusDays(1))
                    .requestEndDate(LocalDate.now().plusDays(3))
                    .quantity(2)
                    .status(RentalStatus.PENDING)
                    .build();

            equipmentItems = List.of(
                    EquipmentItem.builder().equipmentItemId(101L).status(EquipmentStatus.AVAILABLE).build(),
                    EquipmentItem.builder().equipmentItemId(102L).status(EquipmentStatus.AVAILABLE).build()
            );
        }
        // 헬퍼 메서드
        private UpdateRentalStatusDto buildDto(String status, String rejectReason) {
            return UpdateRentalStatusDto.builder()
                    .equipmentId(equipment.getEquipmentId())
                    .newStatus(status)
                    .rejectReason(rejectReason)
                    .build();
        }

        @Test
        @DisplayName("성공 - APPROVED 상태 업데이트")
        void approved() {
            UpdateRentalStatusDto dto = buildDto("APPROVED", null);

            when(rentalRepository.findById(1L)).thenReturn(Optional.of(rental));
            when(equipmentItemRepository.findAvailableItemsForUpdate(anyLong(), any(Pageable.class)))
                    .thenReturn(equipmentItems);
            when(equipmentItemRepository.approveRental(anyList())).thenReturn(equipmentItems.size());
            when(memberRepository.findById(1L)).thenReturn(Optional.of(member));

            rentalService.updateRentalStatus(dto, rental.getRentalId(), member.getMemberId());

            assertThat(rental.getStatus()).isEqualTo(RentalStatus.APPROVED);

            verify(equipmentItemRepository).approveRental(anyList());
            verify(rentalItemRepository).saveAll(anyList());
            verify(equipmentItemHistoryRepository).saveAll(anyList());
        }

        @Test
        @DisplayName("성공 - 승인 알림 및 재고 0 알림 발생")
        void approvedNotification() {
            // given
            UpdateRentalStatusDto dto = buildDto("APPROVED", null);
            when(rentalRepository.findById(1L)).thenReturn(Optional.of(rental));
            when(equipmentItemRepository.findAvailableItemsForUpdate(anyLong(), any(Pageable.class)))
                    .thenReturn(equipmentItems);
            when(equipmentItemRepository.approveRental(anyList())).thenReturn(equipmentItems.size());
            when(memberRepository.findById(1L)).thenReturn(Optional.of(member));
            when(equipmentItemRepository.countAvailableByEquipmentId(equipment.getEquipmentId())).thenReturn(0);

            // when
            rentalService.updateRentalStatus(dto, rental.getRentalId(), member.getMemberId());

            // then
            verify(notificationService, times(1)).createNotification(
                    eq(rental.getMember()),
                    eq(NotificationType.RENTAL_APPROVED),
                    argThat(msg -> msg != null && msg.contains("승인")), // 문자열 포함 체크
                    isNull()
            );

            verify(notificationService, times(1)).notifyManagersAndAdmins(
                    eq(equipment.getSubCategory().getCategory()),
                    eq(NotificationType.EQUIPMENT_OUT_OF_STOCK),
                    argThat(msg -> msg != null && msg.contains("재고가 0")), // 문자열 포함 체크
                    isNull()
            );
        }

        @Test
        @DisplayName("성공 - CANCELLED 상태 업데이트")
        void cancelled() {
            UpdateRentalStatusDto dto = buildDto("CANCELLED", null);

            when(rentalRepository.findById(1L)).thenReturn(Optional.of(rental));

            rentalService.updateRentalStatus(dto, rental.getRentalId(), member.getMemberId());

            assertThat(rental.getStatus()).isEqualTo(RentalStatus.CANCELLED);

            verify(equipmentItemRepository, never()).approveRental(anyList());
            verify(rentalItemRepository, never()).saveAll(anyList());
            verify(equipmentItemHistoryRepository, never()).saveAll(anyList());
        }

        @Test
        @DisplayName("성공 - REJECTED 상태 업데이트")
        void rejected() {
            UpdateRentalStatusDto dto = buildDto("REJECTED", "재고 없음");

            when(rentalRepository.findById(1L)).thenReturn(Optional.of(rental));

            rentalService.updateRentalStatus(dto, rental.getRentalId(), member.getMemberId());

            assertThat(rental.getStatus()).isEqualTo(RentalStatus.REJECTED);
            assertThat(rental.getRejectReason()).isEqualTo("재고 없음");

            verify(equipmentItemRepository, never()).approveRental(anyList());
            verify(rentalItemRepository, never()).saveAll(anyList());
            verify(equipmentItemHistoryRepository, never()).saveAll(anyList());
        }

        @Test
        @DisplayName("예외 - 존재하지 않는 rentalId")
        void rentalNotFound() {
            UpdateRentalStatusDto dto = buildDto("APPROVED", null);

            when(rentalRepository.findById(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> rentalService.updateRentalStatus(dto, 999L, member.getMemberId()))
                    .isInstanceOf(CustomException.class)
                    .extracting("errorType")
                    .isEqualTo(ErrorType.RENTAL_NOT_FOUND);
        }

        @Test
        @DisplayName("예외 - 존재하지 않는 memberId")
        void userNotFound() {
            UpdateRentalStatusDto dto = buildDto("APPROVED", null);

            when(rentalRepository.findById(rental.getRentalId())).thenReturn(Optional.of(rental));
            when(equipmentItemRepository.findAvailableItemsForUpdate(anyLong(), any(Pageable.class)))
                    .thenReturn(equipmentItems);
            when(equipmentItemRepository.approveRental(anyList())).thenReturn(equipmentItems.size());
            when(memberRepository.findById(999L)).thenReturn(Optional.empty());


            assertThatThrownBy(() -> rentalService.updateRentalStatus(dto, rental.getRentalId(), 999L))
                    .isInstanceOf(CustomException.class)
                    .extracting("errorType")
                    .isEqualTo(ErrorType.USER_NOT_FOUND);
        }

        @Test
        @DisplayName("예외 - 대여 시작일 경과")
        void rentalStartDatePassed() {
            rental = Rental.builder()
                    .rentalId(rental.getRentalId())
                    .member(rental.getMember())
                    .equipment(rental.getEquipment())
                    .requestStartDate(LocalDate.now().minusDays(1)) // 오늘 이전 날짜
                    .requestEndDate(rental.getRequestEndDate())
                    .quantity(rental.getQuantity())
                    .status(rental.getStatus())
                    .build();

            UpdateRentalStatusDto dto = buildDto("APPROVED", null);

            when(rentalRepository.findById(rental.getRentalId())).thenReturn(Optional.of(rental));

            assertThatThrownBy(() -> rentalService.updateRentalStatus(dto, rental.getRentalId(), member.getMemberId()))
                    .isInstanceOf(CustomException.class)
                    .extracting("errorType")
                    .isEqualTo(ErrorType.RENTAL_START_DATE_PASSED);
        }

        @Test
        @DisplayName("예외 - 재고 부족")
        void insufficientStock() {
            UpdateRentalStatusDto dto = buildDto("APPROVED", null);

            when(rentalRepository.findById(rental.getRentalId())).thenReturn(Optional.of(rental));
            when(equipmentItemRepository.findAvailableItemsForUpdate(anyLong(), any(Pageable.class)))
                    .thenReturn(List.of(equipmentItems.get(0))); // 필요한 수량보다 적음, 필요 재고 2개

            assertThatThrownBy(() -> rentalService.updateRentalStatus(dto, rental.getRentalId(), member.getMemberId()))
                    .isInstanceOf(CustomException.class)
                    .extracting("errorType")
                    .isEqualTo(ErrorType.EQUIPMENT_ITEM_INSUFFICIENT_STOCK);
        }

        @Test
        @DisplayName("예외 - 승인 수량 mismatch")
        void partialUpdate() {
            UpdateRentalStatusDto dto = buildDto("APPROVED", null);

            when(rentalRepository.findById(rental.getRentalId())).thenReturn(Optional.of(rental));
            when(equipmentItemRepository.findAvailableItemsForUpdate(anyLong(), any(Pageable.class)))
                    .thenReturn(equipmentItems);
            when(equipmentItemRepository.approveRental(anyList())).thenReturn(999); // 실제 수량은 2, mismatch

            assertThatThrownBy(() -> rentalService.updateRentalStatus(dto, rental.getRentalId(), member.getMemberId()))
                    .isInstanceOf(CustomException.class)
                    .extracting("errorType")
                    .isEqualTo(ErrorType.PARTIAL_UPDATE);
        }
    }

    @Nested
    @DisplayName("getUserRentalItemList 메서드 테스트")
    class getUserRentalItemList {
        private Member member;
        private Rental rental;
        private SearchParamDto paramDto;
        private Pageable pageable;

        @BeforeEach
        void setUp() {
            member = Member.builder()
                    .memberId(1L)
                    .name("TestUser")
                    .build();

            rental = Rental.builder()
                    .rentalId(1L)
                    .member(member)
                    .status(RentalStatus.APPROVED)
                    .build();

            paramDto = SearchParamDto.builder().page(1).size(10).build();
            pageable = paramDto.getPageable();
        }

        @Test
        @DisplayName("성공 - PageResponseDto 반환")
        void success() {
            UserRentalItemDto dto1 = UserRentalItemDto.builder()
                    .rentalItemId(101L)
                    .rentalId(rental.getRentalId())
                    .thumbnailUrl("thumb1.png")
                    .category("전자기기")
                    .subCategory("노트북")
                    .model("MacBook Pro")
                    .serialName("MBP-001")
                    .startDate(LocalDate.now())
                    .endDate(LocalDate.now().plusDays(3))
                    .actualReturnDate(null)
                    .status(RentalItemStatus.RENTED)
                    .isExtended(false)
                    .build();

            UserRentalItemDto dto2 = UserRentalItemDto.builder()
                    .rentalItemId(102L)
                    .rentalId(rental.getRentalId())
                    .thumbnailUrl("thumb2.png")
                    .category("전자기기")
                    .subCategory("카메라")
                    .model("Canon R6")
                    .serialName("CAM-001")
                    .startDate(LocalDate.now())
                    .endDate(LocalDate.now().plusDays(3))
                    .actualReturnDate(null)
                    .status(RentalItemStatus.RENTED)
                    .isExtended(false)
                    .build();

            Page<UserRentalItemDto> stubPage = new PageImpl<>(List.of(dto1, dto2), pageable, 2);

            when(rentalRepository.findById(rental.getRentalId())).thenReturn(Optional.of(rental));
            when(rentalItemRepository.findUserRentalItems(paramDto, pageable, rental.getRentalId(), member.getMemberId()))
                    .thenReturn(stubPage);

            PageResponseDto<UserRentalItemDto> response = rentalService.getUserRentalItemList(paramDto, rental.getRentalId(), member.getMemberId());

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
        @DisplayName("예외 - 존재하지 않는 rentalId")
        void rentalNotFound() {
            when(rentalRepository.findById(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> rentalService.getUserRentalItemList(paramDto, 999L, member.getMemberId()))
                    .isInstanceOf(CustomException.class)
                    .extracting("errorType")
                    .isEqualTo(ErrorType.RENTAL_NOT_FOUND);
        }

        @Test
        @DisplayName("예외 - memberId 불일치")
        void accessDenied() {
            Rental otherRental = Rental.builder()
                    .rentalId(2L)
                    .member(Member.builder().memberId(2L).build())
                    .status(RentalStatus.APPROVED)
                    .build();

            when(rentalRepository.findById(otherRental.getRentalId())).thenReturn(Optional.of(otherRental));

            assertThatThrownBy(() -> rentalService.getUserRentalItemList(paramDto, otherRental.getRentalId(), member.getMemberId()))
                    .isInstanceOf(CustomException.class)
                    .extracting("errorType")
                    .isEqualTo(ErrorType.RENTAL_ACCESS_DENIED);
        }

        @Test
        @DisplayName("예외 - rental 상태가 APPROVED 아님")
        void rentalNotApproved() {
            Rental pendingRental = Rental.builder()
                    .rentalId(3L)
                    .member(member)
                    .status(RentalStatus.PENDING)
                    .build();

            when(rentalRepository.findById(pendingRental.getRentalId())).thenReturn(Optional.of(pendingRental));

            assertThatThrownBy(() -> rentalService.getUserRentalItemList(paramDto, pendingRental.getRentalId(), member.getMemberId()))
                    .isInstanceOf(CustomException.class)
                    .extracting("errorType")
                    .isEqualTo(ErrorType.RENTAL_NOT_APPROVED);
        }
    }
}
