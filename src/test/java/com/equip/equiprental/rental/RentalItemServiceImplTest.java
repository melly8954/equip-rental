package com.equip.equiprental.rental;


import com.equip.equiprental.common.dto.PageResponseDto;
import com.equip.equiprental.common.dto.SearchParamDto;
import com.equip.equiprental.common.exception.CustomException;
import com.equip.equiprental.common.exception.ErrorType;
import com.equip.equiprental.equipment.domain.*;
import com.equip.equiprental.equipment.repository.EquipmentItemHistoryRepository;
import com.equip.equiprental.member.domain.Member;
import com.equip.equiprental.member.repository.MemberRepository;
import com.equip.equiprental.notification.domain.NotificationType;
import com.equip.equiprental.notification.service.iface.NotificationService;
import com.equip.equiprental.rental.domain.*;
import com.equip.equiprental.rental.dto.AdminRentalItemDto;
import com.equip.equiprental.rental.dto.ExtendRentalItemDto;
import com.equip.equiprental.rental.repository.RentalItemOverdueRepository;
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
import java.util.Collections;
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
    @Mock private RentalItemOverdueRepository rentalItemOverdueRepository;
    @Mock private MemberRepository memberRepository;
    @Mock private NotificationService notificationService;

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
        @DisplayName("실패 - 존재하지 않는 RentalItem")
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

    @Nested
    @DisplayName("returnRentalItem 메서드 테스트")
    class returnRentalItem {
        Long rentalItemId = 1L;
        Long memberId = 100L;

        @Test
        @DisplayName("성공 - 일부 아이템만 반납")
        void returnRentalItem_partialReturn() {
            // given
            Rental rental = Rental.builder().rentalId(10L).status(RentalStatus.APPROVED).build();
            EquipmentItem item1Equip = EquipmentItem.builder().status(EquipmentStatus.RENTED).build();
            EquipmentItem item2Equip = EquipmentItem.builder().status(EquipmentStatus.RENTED).build();

            RentalItem item1 = RentalItem.builder()
                    .rentalItemId(1L)
                    .rental(rental)
                    .equipmentItem(item1Equip)
                    .status(RentalItemStatus.RENTED)
                    .build();

            RentalItem item2 = RentalItem.builder()
                    .rentalItemId(2L)
                    .rental(rental)
                    .equipmentItem(item2Equip)
                    .status(RentalItemStatus.RENTED)
                    .build();

            List<RentalItem> rentalItems = List.of(item1, item2);
            Member member = Member.builder().memberId(memberId).build();

            when(rentalItemRepository.findById(1L)).thenReturn(Optional.of(item1));
            when(rentalItemRepository.findByRental_RentalId(rental.getRentalId())).thenReturn(rentalItems);
            when(memberRepository.findById(memberId)).thenReturn(Optional.of(member));
            when(rentalItemOverdueRepository.findByRentalItem(item1)).thenReturn(Optional.empty());

            // when
            rentalItemService.returnRentalItem(1L, memberId);

            // then
            assertThat(item1.getStatus()).isEqualTo(RentalItemStatus.RETURNED);
            assertThat(rental.getStatus()).isEqualTo(RentalStatus.APPROVED); // 전체가 반납되지 않았으므로 유지

            verify(equipmentItemHistoryRepository).save(any(EquipmentItemHistory.class));
        }

        @Test
        @DisplayName("성공 - 모든 아이템 반납")
        void returnRentalItem_allReturned() {
            // given
            Rental rental = Rental.builder()
                    .rentalId(10L)
                    .status(RentalStatus.APPROVED)
                    .build();

            Equipment equipment = Equipment.builder()
                    .equipmentId(1L)
                    .model("MacBook Pro")
                    .build();

            EquipmentItem item1Equip = EquipmentItem.builder()
                    .equipmentItemId(101L)
                    .equipment(equipment)
                    .status(EquipmentStatus.RENTED)
                    .build();

            EquipmentItem item2Equip = EquipmentItem.builder()
                    .equipmentItemId(102L)
                    .equipment(equipment)
                    .status(EquipmentStatus.AVAILABLE)
                    .build();

            RentalItem item1 = RentalItem.builder()
                    .rentalItemId(1L)
                    .rental(rental)
                    .equipmentItem(item1Equip)
                    .status(RentalItemStatus.RENTED)
                    .build();

            RentalItem item2 = RentalItem.builder()
                    .rentalItemId(2L)
                    .rental(rental)
                    .equipmentItem(item2Equip)
                    .status(RentalItemStatus.RETURNED)
                    .build();

            List<RentalItem> rentalItems = List.of(item1, item2);
            Member member = Member.builder().memberId(memberId).build();

            when(rentalItemRepository.findById(1L)).thenReturn(Optional.of(item1));
            when(rentalItemRepository.findByRental_RentalId(rental.getRentalId())).thenReturn(rentalItems);
            when(memberRepository.findById(memberId)).thenReturn(Optional.of(member));
            when(rentalItemOverdueRepository.findByRentalItem(item1)).thenReturn(Optional.empty());

            // when
            rentalItemService.returnRentalItem(1L, memberId);

            // then
            assertThat(item1.getStatus()).isEqualTo(RentalItemStatus.RETURNED);
            assertThat(rental.getStatus()).isEqualTo(RentalStatus.COMPLETED); // 모든 아이템 반납 완료

            verify(equipmentItemHistoryRepository).save(any(EquipmentItemHistory.class));
        }

        @Test
        @DisplayName("성공 - 연체 발생 시 overdue actualReturnDate update")
        void returnRentalItem_overdueUpdated() {
            Rental rental = Rental.builder()
                    .rentalId(10L)
                    .build();

            Equipment equipment = Equipment.builder()
                    .equipmentId(1L)
                    .model("MacBook Pro")
                    .build();

            EquipmentItem equipmentItem = EquipmentItem.builder()
                    .equipment(equipment)
                    .status(EquipmentStatus.RENTED)
                    .build();

            RentalItem item = RentalItem.builder()
                    .rentalItemId(1L)
                    .rental(rental)
                    .equipmentItem(equipmentItem)
                    .status(RentalItemStatus.OVERDUE) // 스케줄러에서 이미 OVERDUE
                    .endDate(LocalDate.now().minusDays(3))
                    .build();

            RentalItemOverdue overdue = mock(RentalItemOverdue.class);

            when(rentalItemRepository.findById(1L)).thenReturn(Optional.of(item));
            when(memberRepository.findById(memberId)).thenReturn(Optional.of(Member.builder().memberId(memberId).build()));
            when(rentalItemOverdueRepository.findByRentalItem(item)).thenReturn(Optional.of(overdue));

            rentalItemService.returnRentalItem(1L, memberId);

            verify(overdue).markReturned(LocalDate.now());
            verify(rentalItemOverdueRepository).save(overdue);
        }

        @Test
        @DisplayName("성공 - 모든 아이템 반납 시 알림 발생")
        void returnRentalItem_notificationTriggered() {
            // given
            Member member = Member.builder()
                    .memberId(memberId)
                    .build();

            Rental rental = Rental.builder()
                    .rentalId(10L)
                    .status(RentalStatus.APPROVED)
                    .member(member)
                    .build();

            Equipment equipment = Equipment.builder()
                    .equipmentId(1L)
                    .model("MacBook Pro")
                    .build();

            EquipmentItem item1Equip = EquipmentItem.builder()
                    .equipment(equipment)
                    .status(EquipmentStatus.RENTED)
                    .build();
            EquipmentItem item2Equip = EquipmentItem.builder()
                    .equipment(equipment)
                    .status(EquipmentStatus.AVAILABLE)
                    .build();

            RentalItem item1 = RentalItem.builder()
                    .rentalItemId(1L)
                    .rental(rental)
                    .equipmentItem(item1Equip)
                    .status(RentalItemStatus.RENTED)
                    .build();
            RentalItem item2 = RentalItem.builder()
                    .rentalItemId(2L)
                    .rental(rental)
                    .equipmentItem(item2Equip)
                    .status(RentalItemStatus.RETURNED)
                    .build();

            List<RentalItem> rentalItems = List.of(item1, item2);

            when(rentalItemRepository.findById(1L)).thenReturn(Optional.of(item1));
            when(rentalItemRepository.findByRental_RentalId(rental.getRentalId())).thenReturn(rentalItems);
            when(memberRepository.findById(memberId)).thenReturn(Optional.of(member));
            when(rentalItemOverdueRepository.findByRentalItem(item1)).thenReturn(Optional.empty());

            // when
            rentalItemService.returnRentalItem(1L, memberId);

            // then
            verify(notificationService, times(1)).createNotification(
                    eq(member),
                    eq(NotificationType.RENTAL_RETURNED),
                    contains("모두 반납 완료"),
                    isNull()
            );
        }

        @Test
        @DisplayName("예외 - 존재하지 않는 RentalItem")
        void returnRentalItem_rentalNotFound() {
            when(rentalItemRepository.findById(rentalItemId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> rentalItemService.returnRentalItem(rentalItemId, memberId))
                    .isInstanceOf(CustomException.class)
                    .extracting("errorType")
                    .isEqualTo(ErrorType.RENTAL_NOT_FOUND);
        }

        @Test
        @DisplayName("예외 - 존재하지 않는 Member")
        void returnRentalItem_memberNotFound() {
            Equipment equipment = Equipment.builder()
                    .equipmentId(1L)
                    .model("MacBook Pro")
                    .build();

            EquipmentItem equipmentItem = EquipmentItem.builder()
                    .status(EquipmentStatus.RENTED)
                    .equipment(equipment)
                    .build();

            Rental rental = Rental.builder()
                    .rentalId(10L)
                    .build();

            RentalItem item = RentalItem.builder().rentalItemId(rentalItemId)
                    .equipmentItem(equipmentItem)
                    .endDate(LocalDate.now().plusDays(2))
                    .rental(rental)
                    .build();

            when(rentalItemRepository.findById(rentalItemId)).thenReturn(Optional.of(item));
            when(memberRepository.findById(memberId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> rentalItemService.returnRentalItem(rentalItemId, memberId))
                    .isInstanceOf(CustomException.class)
                    .extracting("errorType")
                    .isEqualTo(ErrorType.USER_NOT_FOUND);
        }
    }

    @Nested
    @DisplayName("updateOverdueStatus 메서드 테스트")
    class updateOverdueStatus {
        @Test
        @DisplayName("성공 - 연체 상황 발생")
        void updateOverdueStatus_success() {
            Member member = Member.builder()
                    .memberId(1L)
                    .name("Tester")
                    .build();

            Category category = Category.builder()
                    .categoryId(1L)
                    .label("노트북")
                    .build();

            SubCategory subCategory = SubCategory.builder()
                    .subCategoryId(1L)
                    .label("맥북")
                    .category(category)
                    .build();

            Equipment equipment = Equipment.builder()
                    .equipmentId(1L)
                    .model("MacBook Pro")
                    .subCategory(subCategory)
                    .build();

            Rental rental = Rental.builder()
                    .rentalId(10L)
                    .member(member)
                    .equipment(equipment)
                    .build();

            RentalItem item1 = RentalItem.builder()
                    .rentalItemId(1L)
                    .status(RentalItemStatus.RENTED)
                    .endDate(LocalDate.now().minusDays(2))
                    .rental(rental)
                    .build();

            RentalItem item2 = RentalItem.builder()
                    .rentalItemId(2L)
                    .status(RentalItemStatus.RENTED)
                    .endDate(LocalDate.now().minusDays(1))
                    .rental(rental)
                    .build();

            when(rentalItemRepository.findByStatusAndEndDateBefore(RentalItemStatus.RENTED, LocalDate.now()))
                    .thenReturn(List.of(item1, item2));

            // when
            rentalItemService.updateOverdueStatus();

            // then
            assertThat(item1.getStatus()).isEqualTo(RentalItemStatus.OVERDUE);
            assertThat(item2.getStatus()).isEqualTo(RentalItemStatus.OVERDUE);
            
            verify(rentalItemOverdueRepository, times(2)).save(any(RentalItemOverdue.class));

            // 사용자 알림
            verify(notificationService, times(2)).createNotification(
                    eq(member),
                    eq(NotificationType.RENTAL_OVERDUE),
                    contains("연체"),
                    isNull()
            );

            // 관리자/매니저 알림
            verify(notificationService, times(2)).notifyManagersAndAdmins(
                    eq(category),
                    eq(NotificationType.RENTAL_OVERDUE),
                    contains(member.getName()),
                    isNull()
            );
        }

        @Test
        @DisplayName("성공 - 연체 미발생 ")
        void updateOverdueStatus_noOverdueItems() {
            // given
            when(rentalItemRepository.findByStatusAndEndDateBefore(RentalItemStatus.RENTED, LocalDate.now()))
                    .thenReturn(Collections.emptyList());

            // when
            rentalItemService.updateOverdueStatus();

            // then
            verify(rentalItemOverdueRepository, never()).save(any(RentalItemOverdue.class));
        }
    }
}
