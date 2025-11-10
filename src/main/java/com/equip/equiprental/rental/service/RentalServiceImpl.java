package com.equip.equiprental.rental.service;

import com.equip.equiprental.common.dto.PageResponseDto;
import com.equip.equiprental.common.dto.SearchParamDto;
import com.equip.equiprental.common.exception.CustomException;
import com.equip.equiprental.common.exception.ErrorType;
import com.equip.equiprental.equipment.domain.Equipment;
import com.equip.equiprental.equipment.domain.EquipmentItem;
import com.equip.equiprental.equipment.domain.EquipmentItemHistory;
import com.equip.equiprental.equipment.domain.EquipmentStatus;
import com.equip.equiprental.equipment.repository.EquipmentItemHistoryRepository;
import com.equip.equiprental.equipment.repository.EquipmentItemRepository;
import com.equip.equiprental.equipment.repository.EquipmentRepository;
import com.equip.equiprental.member.domain.Member;
import com.equip.equiprental.member.repository.MemberRepository;
import com.equip.equiprental.notification.domain.NotificationType;
import com.equip.equiprental.notification.service.iface.NotificationService;
import com.equip.equiprental.rental.domain.Rental;
import com.equip.equiprental.rental.domain.RentalItem;
import com.equip.equiprental.rental.domain.RentalItemStatus;
import com.equip.equiprental.rental.domain.RentalStatus;
import com.equip.equiprental.rental.dto.*;
import com.equip.equiprental.rental.repository.RentalItemRepository;
import com.equip.equiprental.rental.repository.RentalRepository;
import com.equip.equiprental.rental.service.iface.RentalService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.stream.IntStream;

@Service
@RequiredArgsConstructor
public class RentalServiceImpl implements RentalService {

    private final MemberRepository memberRepository;
    private final EquipmentRepository equipmentRepository;
    private final EquipmentItemRepository equipmentItemRepository;
    private final EquipmentItemHistoryRepository equipmentItemHistoryRepository;
    private final RentalRepository rentalRepository;
    private final RentalItemRepository rentalItemRepository;
    private final NotificationService notificationService;

    @Override
    @Transactional
    public RentalResponseDto requestRental(RentalRequestDto dto, Long currentUserId) {
        Member member = memberRepository.findByMemberId(currentUserId)
                .orElseThrow(() -> new CustomException(ErrorType.NOT_FOUND, "회원 정보를 찾을 수 없습니다."));

        Equipment equipment = equipmentRepository.findById(dto.getEquipmentId())
                .orElseThrow(() -> new CustomException(ErrorType.NOT_FOUND, "해당 정보로 등록된 기자재가 존재하지 않습니다."));

        int availableStock = equipmentItemRepository.countAvailableByEquipmentId(dto.getEquipmentId());

        // 대여 날짜 검증
        LocalDate today = LocalDate.now();
        if (dto.getStartDate().isBefore(today)) {
            throw new CustomException(ErrorType.BAD_REQUEST, "대여 시작일은 오늘 이후여야 합니다.");
        }

        if (dto.getEndDate().isBefore(dto.getStartDate())) {
            throw new CustomException(ErrorType.BAD_REQUEST, "대여 종료일은 대여 시작일과 같거나 이후여야 합니다.");
        }

        // 수량 검증
        if (dto.getQuantity() <= 0 || dto.getQuantity() > availableStock) {
            throw new CustomException(ErrorType.BAD_REQUEST, "대여 수량이 남은 재고보다 많습니다.");
        }

        Rental rental = Rental.builder()
                .member(member)
                .equipment(equipment)
                .requestStartDate(dto.getStartDate())
                .requestEndDate(dto.getEndDate())
                .quantity(dto.getQuantity())
                .rentalReason(dto.getRentalReason())
                .status(RentalStatus.PENDING)
                .build();

        rentalRepository.save(rental);

        notificationService.notifyManagersAndAdmins(
                equipment.getSubCategory().getCategory(),
                NotificationType.RENTAL_REQUEST,
                rental.getMember().getName() + "님이 기자재 '" + equipment.getModel() + "' 대여 신청",
                null
        );

        return RentalResponseDto.builder()
                .rentalId(rental.getRentalId())
                .equipmentId(equipment.getEquipmentId())
                .quantity(rental.getQuantity())
                .status(rental.getStatus())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponseDto<AdminRentalDto> getAdminRentalList(RentalFilter paramDto) {
        Pageable pageable = paramDto.getPageable();

        Page<AdminRentalDto> dtosPage = rentalRepository.findAdminRentals(paramDto, pageable);

        return PageResponseDto.<AdminRentalDto>builder()
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
    @Transactional(readOnly = true)
    public PageResponseDto<UserRentalDto> getUserRentalList(RentalFilter paramDto, Long currentUserId) {
        Pageable pageable = paramDto.getPageable();

        Page<UserRentalDto> dtosPage = rentalRepository.findUserRentals(paramDto, pageable, currentUserId);

        return PageResponseDto.<UserRentalDto>builder()
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
    public void updateRentalStatus(UpdateRentalStatusDto dto, Long rentalId, Long memberId) {
        Rental rental = rentalRepository.findById(rentalId)
                .orElseThrow(() -> new CustomException(ErrorType.NOT_FOUND, "해당 정보로 등록된 대여 신청내역이 존재하지 않습니다."));

        rental.updateStatus(dto.getNewStatus());

        // 거절(REJECTED) 처리
        Equipment equipment = rental.getEquipment();

        if (dto.getNewStatus() == RentalStatus.REJECTED) {
            rental.updateRejectReason(dto.getRejectReason());

            String msg = equipment.getModel() + " 대여가 거절되었습니다.";
            notificationService.createNotification(rental.getMember(), NotificationType.RENTAL_REJECTED, msg, null);

            return;
        }

        if (dto.getNewStatus() == RentalStatus.CANCELLED) {
            rental.updateStatus(RentalStatus.CANCELLED);
            return;
        }

        // 승인(APPROVED) 처리
        Pageable limit = PageRequest.of(0, rental.getQuantity());

        List<EquipmentItem> equipmentItems = equipmentItemRepository.findAvailableItemsForUpdate(dto.getEquipmentId(),limit);

        if (!rental.getRequestStartDate().isEqual(LocalDate.now())) {
            throw new CustomException(ErrorType.CONFLICT, "대여 승인은 대여 시작일에만 승인할 수 있습니다.");
        }

        if (rental.getRequestStartDate().isBefore(LocalDate.now())) {
            throw new CustomException(ErrorType.CONFLICT, "대여 시작일이 이미 지나 승인할 수 없습니다.");
        }

        if (equipmentItems.size() < rental.getQuantity()) {
            throw new CustomException(ErrorType.CONFLICT, "해당 기자재 모델의 대여 가능 재고가 부족합니다.");
        }

        List<Long> itemIds = equipmentItems.stream()
                .map(EquipmentItem::getEquipmentItemId)
                .toList();
        
        // bulk update 전에 상태 저장
        List<EquipmentStatus> oldStatuses = equipmentItems.stream()
                .map(EquipmentItem::getStatus)
                .toList();

        int updatedCount = equipmentItemRepository.approveRental(itemIds);
        if (updatedCount != itemIds.size()) {
            throw new CustomException(ErrorType.CONFLICT, "요청한 모든 기자재 아이템 상태를 업데이트하지 못했습니다.");
        }

        List<RentalItem> rentalItems = equipmentItems.stream()
                .map(item -> RentalItem.builder()
                        .rental(rental)
                        .equipmentItem(item)
                        .startDate(rental.getRequestStartDate())
                        .endDate(rental.getRequestEndDate())
                        .status(RentalItemStatus.RENTED)
                        .actualReturnDate(null)
                        .isExtended(false)
                        .build())
                .toList();
        rentalItemRepository.saveAll(rentalItems);

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(ErrorType.NOT_FOUND, "회원 정보를 찾을 수 없습니다."));

        List<EquipmentItemHistory> histories = IntStream.range(0, equipmentItems.size())
                .mapToObj(i -> EquipmentItemHistory.builder()
                        .item(equipmentItems.get(i))
                        .changedBy(member)
                        .oldStatus(oldStatuses.get(i))
                        .newStatus(EquipmentStatus.RENTED)
                        .rentedUser(rental.getMember())
                        .build())
                .toList();
        equipmentItemHistoryRepository.saveAll(histories);

        String msg = equipment.getModel() + " 대여가 승인되었습니다.";
        notificationService.createNotification(rental.getMember(), NotificationType.RENTAL_APPROVED, msg, null);

        // 승인 후 재고 체크
        int remainingStock = equipmentItemRepository.countAvailableByEquipmentId(equipment.getEquipmentId());
        if (remainingStock == 0) {
            String stockMsg = "'" + equipment.getModel() + "'" + " 기자재 재고가 0이 되었습니다.";

            notificationService.notifyManagersAndAdmins(
                    equipment.getSubCategory().getCategory(),
                    NotificationType.EQUIPMENT_OUT_OF_STOCK,
                    stockMsg,
                    null
            );
        }
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponseDto<UserRentalItemDto> getUserRentalItemList(SearchParamDto paramDto, Long rentalId, Long currentUserId) {
        Pageable pageable = paramDto.getPageable();

        Rental rental = rentalRepository.findById(rentalId)
                .orElseThrow(() -> new CustomException(ErrorType.NOT_FOUND, "해당 정보로 등록된 대여 신청내역이 존재하지 않습니다."));

        if(!Objects.equals(rental.getMember().getMemberId(), currentUserId)){
            throw new CustomException(ErrorType.BAD_REQUEST, "해당 대여 내역을 조회할 권한이 없습니다.");
        }

        if(rental.getStatus() != RentalStatus.APPROVED){
            throw new CustomException(ErrorType.CONFLICT, "해당 기자재는 아직 대여 승인이 완료되지 않았습니다.");
        }

        Page<UserRentalItemDto> dtosPage = rentalItemRepository.findUserRentalItems(pageable, rentalId, currentUserId);

        return PageResponseDto.<UserRentalItemDto>builder()
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
    @Transactional(readOnly = true)
    public PageResponseDto<ReturnedRentalItemDto> getReturnRentalItemList(SearchParamDto paramDto, Long rentalId, Long currentUserId) {
        Pageable pageable = paramDto.getPageable();

        Rental rental = rentalRepository.findById(rentalId)
                .orElseThrow(() -> new CustomException(ErrorType.NOT_FOUND, "해당 정보로 등록된 대여 신청내역이 존재하지 않습니다."));

        if(!Objects.equals(rental.getMember().getMemberId(), currentUserId)){
            throw new CustomException(ErrorType.BAD_REQUEST, "해당 대여 내역을 조회할 권한이 없습니다.");
        }

        if(rental.getStatus() != RentalStatus.COMPLETED){
            throw new CustomException(ErrorType.CONFLICT, "해당 대여 신청은 아직 반납 완료되지 않았습니다.");
        }

        Page<ReturnedRentalItemDto> dtosPage = rentalItemRepository.findReturnRentalItems(pageable, rentalId, currentUserId);

        return PageResponseDto.<ReturnedRentalItemDto>builder()
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
}
