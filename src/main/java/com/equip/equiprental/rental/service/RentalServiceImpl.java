package com.equip.equiprental.rental.service;

import com.equip.equiprental.common.exception.CustomException;
import com.equip.equiprental.common.exception.ErrorType;
import com.equip.equiprental.equipment.domain.Equipment;
import com.equip.equiprental.equipment.repository.EquipmentRepository;
import com.equip.equiprental.member.domain.Member;
import com.equip.equiprental.member.repository.MemberRepository;
import com.equip.equiprental.rental.domain.Rental;
import com.equip.equiprental.rental.domain.RentalStatus;
import com.equip.equiprental.rental.dto.RentalRequestDto;
import com.equip.equiprental.rental.dto.RentalResponseDto;
import com.equip.equiprental.rental.repository.RentalRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RentalServiceImpl implements RentalService{

    private final MemberRepository memberRepository;
    private final EquipmentRepository equipmentRepository;
    private final RentalRepository rentalRepository;

    @Override
    @Transactional
    public RentalResponseDto requestRental(RentalRequestDto dto, Long memberId) {
        Member member = memberRepository.findByMemberId(memberId)
                .orElseThrow(() -> new CustomException(ErrorType.USER_NOT_FOUND));

        Equipment equipment = equipmentRepository.findById(dto.getEquipmentId())
                .orElseThrow(() -> new CustomException(ErrorType.EQUIPMENT_NOT_FOUND));

        Rental rental = Rental.builder()
                .member(member)
                .equipment(equipment)
                .quantity(dto.getQuantity())
                .rentalReason(dto.getRentalReason())
                .status(RentalStatus.PENDING)
                .build();

        rentalRepository.save(rental);

        return RentalResponseDto.builder()
                .rentalId(rental.getRentalId())
                .equipmentId(equipment.getEquipmentId())
                .quantity(rental.getQuantity())
                .status(rental.getStatus())
                .build();
    }
}
