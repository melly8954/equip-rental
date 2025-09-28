package com.equip.equiprental.dashboard.service;

import com.equip.equiprental.dashboard.dto.KpiItemDto;
import com.equip.equiprental.dashboard.dto.KpiResponseDto;
import com.equip.equiprental.equipment.domain.EquipmentStatus;
import com.equip.equiprental.equipment.repository.EquipmentRepository;
import com.equip.equiprental.rental.domain.RentalItemStatus;
import com.equip.equiprental.rental.repository.RentalItemRepository;
import com.equip.equiprental.rental.repository.RentalRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DashBoardServiceImpl implements DashBoardService {
    private final RentalRepository rentalRepository;
    private final RentalItemRepository rentalItemRepository;
    private final EquipmentRepository equipmentRepository;

    @Override
    @Transactional(readOnly = true)
    public KpiResponseDto getDashBoardKpi() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime firstDayThisMonth = now.withDayOfMonth(1);
        LocalDateTime firstDayNextMonth = firstDayThisMonth.plusMonths(1);
        LocalDateTime firstDayLastMonth = firstDayThisMonth.minusMonths(1);

        int newRequestsThisMonth = rentalRepository.countThisMonth(firstDayThisMonth, firstDayNextMonth);
        int newRequestsLastMonth = rentalRepository.countLastMonth(firstDayLastMonth, firstDayThisMonth);
        String newRequestsChange = calcChangeRate(newRequestsThisMonth, newRequestsLastMonth);

        int approvedThisMonth = rentalRepository.countApprovedThisMonth(firstDayThisMonth, firstDayNextMonth);
        int approvedLastMonth = rentalRepository.countApprovedLastMonth(firstDayLastMonth, firstDayThisMonth);
        String approvedChange = calcChangeRate(approvedThisMonth, approvedLastMonth);

        int pendingCount = rentalRepository.countPendingNow();

        int overdueCount = rentalItemRepository.countOverdueNow();

        List<EquipmentStatus> faultyStatuses = List.of(EquipmentStatus.REPAIRING, EquipmentStatus.LOST);
        int faultyCount = equipmentRepository.countFaultyNow(faultyStatuses);

        // 2. KPI 리스트 생성
        List<KpiItemDto> kpis = Arrays.asList(
                new KpiItemDto("이번 달 신규 대여 신청 건수", newRequestsThisMonth, newRequestsChange),
                new KpiItemDto("이번 달 승인된 대여 건수", approvedThisMonth, approvedChange),
                new KpiItemDto("현재 승인 대기 중인 건수", pendingCount, null),
                new KpiItemDto("현재 대여 연체 중인 건수", overdueCount, null),
                new KpiItemDto("현재 파손 및 분실 장비 건수", faultyCount, null)
        );

        return KpiResponseDto.builder()
                .kpis(kpis)
                .build();
    }

    private String calcChangeRate(int current, int previous) {
        if (previous == 0) {
            return current == 0 ? "0%" : "신규 발생";
        }
        double rate = ((double)(current - previous) / previous) * 100;
        return String.format("%.1f%%", rate);
    }
}
