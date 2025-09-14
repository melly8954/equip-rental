package com.equip.equiprental.rental.service;

import com.equip.equiprental.common.exception.CustomException;
import com.equip.equiprental.common.exception.ErrorType;
import com.equip.equiprental.equipment.domain.Equipment;
import com.equip.equiprental.equipment.domain.EquipmentItem;
import com.equip.equiprental.equipment.repository.EquipmentItemRepository;
import com.equip.equiprental.equipment.repository.EquipmentRepository;
import com.equip.equiprental.member.domain.Member;
import com.equip.equiprental.member.repository.MemberRepository;
import com.equip.equiprental.rental.domain.Rental;
import com.equip.equiprental.rental.domain.RentalItem;
import com.equip.equiprental.rental.domain.RentalStatus;
import com.equip.equiprental.rental.dto.RentalRequestDto;
import com.equip.equiprental.rental.dto.RentalResponseDto;
import com.equip.equiprental.rental.repository.RentalItemRepository;
import com.equip.equiprental.rental.repository.RentalRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RentalServiceImpl implements RentalService{

    private final MemberRepository memberRepository;
    private final EquipmentRepository equipmentRepository;
    private final EquipmentItemRepository equipmentItemRepository;
    private final RentalRepository rentalRepository;
    private final RentalItemRepository rentalItemRepository;

    @Override
    @Transactional
    public RentalResponseDto requestRental(RentalRequestDto dto, Long memberId) {
        Member member = memberRepository.findByMemberId(memberId)
                .orElseThrow(() -> new CustomException(ErrorType.USER_NOT_FOUND));

        Equipment equipment = equipmentRepository.findById(dto.getEquipmentId())
                .orElseThrow(() -> new CustomException(ErrorType.EQUIPMENT_NOT_FOUND));

        List<EquipmentItem> availableItems = equipmentItemRepository
                .findFirstNAvailableByEquipment(equipment.getEquipmentId(), dto.getQuantity());
        if (availableItems.size() < dto.getQuantity()) {
            throw new CustomException(ErrorType.EQUIPMENT_ITEM_NOT_AVAILABLE);
        }

        Rental rental = Rental.builder()
                .member(member)
                .equipment(equipment)
                .rentalReason(dto.getRentalReason())
                .status(RentalStatus.PENDING)
                .build();

        rentalRepository.save(rental);

        List<RentalItem> rentalItems = availableItems.stream()
                .map(item -> RentalItem.builder()
                        .rental(rental)
                        .equipmentItem(item)
                        .startDate(dto.getStartDate())
                        .endDate(dto.getEndDate())
                        .isExtended(false)
                        .build())
                .collect(Collectors.toList());

        rentalItemRepository.saveAll(rentalItems);

        return RentalResponseDto.builder()
                .rentalId(rental.getRentalId())
                .equipmentId(equipment.getEquipmentId())
                .equipmentItemIds(
                        rentalItems.stream()
                                .map(ri -> ri.getEquipmentItem().getEquipmentItemId())
                                .collect(Collectors.toList())
                )
                .status(rental.getStatus())
                .build();
    }
}
