package com.equip.equiprental.rental.service;

import com.equip.equiprental.common.dto.PageResponseDto;
import com.equip.equiprental.common.exception.CustomException;
import com.equip.equiprental.common.exception.ErrorType;
import com.equip.equiprental.equipment.domain.Equipment;
import com.equip.equiprental.equipment.domain.EquipmentItem;
import com.equip.equiprental.equipment.domain.EquipmentItemHistory;
import com.equip.equiprental.equipment.domain.EquipmentStatus;
import com.equip.equiprental.equipment.repository.EquipmentItemHistoryRepository;
import com.equip.equiprental.member.domain.Member;
import com.equip.equiprental.member.domain.MemberRole;
import com.equip.equiprental.member.repository.MemberRepository;
import com.equip.equiprental.notification.domain.NotificationType;
import com.equip.equiprental.notification.service.iface.NotificationService;
import com.equip.equiprental.rental.domain.*;
import com.equip.equiprental.rental.dto.AdminRentalItemDto;
import com.equip.equiprental.rental.dto.ExtendRentalItemDto;
import com.equip.equiprental.rental.dto.RentalFilter;
import com.equip.equiprental.rental.repository.RentalItemOverdueRepository;
import com.equip.equiprental.rental.repository.RentalItemRepository;
import com.equip.equiprental.rental.service.iface.RentalItemService;
import com.equip.equiprental.scope.service.ManagerScopeService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class RentalItemServiceImpl implements RentalItemService {

    private final RentalItemRepository rentalItemRepository;
    private final RentalItemOverdueRepository rentalItemOverdueRepository;
    private final EquipmentItemHistoryRepository equipmentItemHistoryRepository;
    private final MemberRepository memberRepository;
    private final NotificationService notificationService;
    private final ManagerScopeService managerScopeService;

    @Override
    @Transactional(readOnly = true)
    public PageResponseDto<AdminRentalItemDto> getAdminRentalItemLists(RentalFilter paramDto) {
        Pageable pageable = paramDto.getPageable();

        Page<AdminRentalItemDto> dtosPage = rentalItemRepository.findAdminRentalItems(paramDto, pageable);

        return PageResponseDto.<AdminRentalItemDto>builder()
                .content(dtosPage.getContent())
                .page(dtosPage.getNumber() + 1)
                .size(dtosPage.getSize())
                .totalElements(dtosPage.getTotalElements())
                .totalPages(dtosPage.getTotalPages())
                .numberOfElements(dtosPage.getNumberOfElements())
                .first(dtosPage.isFirst())
                .last(dtosPage.isLast())
                .empty(dtosPage.isEmpty())
                .build();
    }

    @Override
    @Transactional
    public void extendRentalItem(Long rentalItem, ExtendRentalItemDto dto, Long memberId) {
        RentalItem item = rentalItemRepository.findById(rentalItem)
                .orElseThrow(() -> new CustomException(ErrorType.RENTAL_NOT_FOUND));

        if (!item.getRental().getMember().getMemberId().equals(memberId)) {
            throw new CustomException(ErrorType.UNAUTHORIZED);
        }

        LocalDate extendedEndDate = item.getEndDate().plusDays(dto.getDays());

        // 엔티티 수정 메서드 호출
        item.extend(extendedEndDate);
    }

    @Override
    @Transactional
    public void returnRentalItem(Long rentalItem, Long memberId) {
        RentalItem item = rentalItemRepository.findById(rentalItem)
                .orElseThrow(() -> new CustomException(ErrorType.RENTAL_NOT_FOUND));

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(ErrorType.USER_NOT_FOUND));

        EquipmentItem equipmentItem = item.getEquipmentItem();
        Rental rental = item.getRental();

        // 매니저 스코프 검사
        if (member.getRole()  == MemberRole.MANAGER &&
                !managerScopeService.canAccessEquipment(equipmentItem.getEquipment().getEquipmentId(), memberId)) {
            throw new CustomException(ErrorType.FORBIDDEN);
        }

        EquipmentStatus oldStatus = equipmentItem.getStatus();

        // 장비 상태 변경(사용 가능) + 실 반납일 저장
        equipmentItem.updateStatus(EquipmentStatus.AVAILABLE);
        item.returnItem(LocalDate.now());

        // 연체 여부 계산 후 연체 테이블 생성
        Optional<RentalItemOverdue> overdueOpt = rentalItemOverdueRepository.findByRentalItem(item);
        if (overdueOpt.isPresent()) {
            RentalItemOverdue overdue = overdueOpt.get();
            overdue.markReturned(LocalDate.now());
            rentalItemOverdueRepository.save(overdue);
        }

        // 모든 아이템이 반납되었으면 Rental 상태 변경
        List<RentalItem> rentalItems = rentalItemRepository.findByRental_RentalId(rental.getRentalId());

        boolean allReturned = rentalItems.stream()
                .allMatch(i -> i.getStatus() == RentalItemStatus.RETURNED);

        if (allReturned) {
            rental.updateStatus(RentalStatus.COMPLETED);

            // 알림 발송
            notificationService.createNotification(
                    rental.getMember(), // 사용자
                    NotificationType.RENTAL_RETURNED,
                    "'" + equipmentItem.getEquipment().getModel() + "' 대여가 모두 반납 완료되었습니다.",
                    null
            );
        }

        // 히스토리 저장
        EquipmentItemHistory history = EquipmentItemHistory.builder()
                .item(equipmentItem)
                .changedBy(member)
                .oldStatus(oldStatus)
                .newStatus(EquipmentStatus.AVAILABLE)
                .rentalStartDate(item.getStartDate())
                .actualReturnDate(item.getActualReturnDate())
                .rentedUser(item.getRental().getMember())
                .build();

        equipmentItemHistoryRepository.save(history);
    }

    @Override
    @Transactional
    public void  updateOverdueStatus() {
        // 연체 대상 RentalItem 조회
        List<RentalItem> overdueItems = rentalItemRepository.findByStatusAndEndDateBefore(RentalItemStatus.RENTED, LocalDate.now());

        for (RentalItem item : overdueItems) {
            // 상태 업데이트
            item.rentalOverdue(RentalItemStatus.OVERDUE);

            // 3. overdue_tbl insert
            RentalItemOverdue overdue = RentalItemOverdue.builder()
                    .rentalItem(item)
                    .plannedEndDate(item.getEndDate())
                    .actualReturnDate(null) // 반납 전이므로 null
                    .overdueDays((int) ChronoUnit.DAYS.between(item.getEndDate(), LocalDate.now()))
                    .build();
            rentalItemOverdueRepository.save(overdue);

            // 알림 발송
            Member renter = item.getRental().getMember();
            Equipment equipment = item.getRental().getEquipment();

            // 사용자 알림
            notificationService.createNotification(
                    renter,
                    NotificationType.RENTAL_OVERDUE,
                    "'" + equipment.getModel() + "' 장비가 연체되었습니다. 빠른 반납 부탁드립니다.",
                    null
            );

            // 관리자/매니저 알림
            notificationService.notifyManagersAndAdmins(
                    equipment.getSubCategory().getCategory(),
                    NotificationType.RENTAL_OVERDUE,
                    renter.getName() + "님이 '" + equipment.getModel() + "' 장비를 연체 중입니다.",
                    null
            );
        }
    }
}
