package com.equip.equiprental.dashboard.service;

import com.equip.equiprental.common.dto.PageResponseDto;
import com.equip.equiprental.common.dto.SearchParamDto;
import com.equip.equiprental.dashboard.dto.*;
import com.equip.equiprental.equipment.domain.Equipment;
import com.equip.equiprental.equipment.domain.EquipmentStatus;
import com.equip.equiprental.equipment.repository.EquipmentRepository;
import com.equip.equiprental.rental.repository.RentalItemRepository;
import com.equip.equiprental.rental.repository.RentalRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

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

    @Override
    @Transactional(readOnly = true)
    public PageResponseDto<ZeroStockDto> getDashBoardZeroStock(SearchParamDto paramDto) {
        Pageable pageable = PageRequest.of(
                paramDto.getPage() - 1,
                paramDto.getSize(),
                Sort.by("subCategory.category.label").ascending()
                        .and(Sort.by("subCategory.label").ascending()
                        .and(Sort.by("modelSequence").descending()))
        );
        Page<Equipment> page = equipmentRepository.findByStock(0, pageable);

        List<ZeroStockDto> content = page.stream()
                .map(e -> ZeroStockDto.builder()
                        .equipmentId(e.getEquipmentId())
                        .category(e.getSubCategory().getCategory().getLabel())
                        .subCategory(e.getSubCategory().getLabel())
                        .model(e.getModel())
                        .build())
                .collect(Collectors.toList());

        return PageResponseDto.<ZeroStockDto>builder()
                .content(content)
                .page(page.getNumber() + 1)
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .numberOfElements(page.getNumberOfElements())
                .first(page.isFirst())
                .last(page.isLast())
                .empty(page.isEmpty())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoryInventoryResponse> getCategoryInventory() {
        List<Equipment> allEquipment  = equipmentRepository.findAllWithCategoryAndSubCategory();

        return allEquipment.stream()
                .collect(Collectors.groupingBy(                      // Map<Key, Value> 반환
                        e -> e.getSubCategory().getCategory(),       // key: Category
                        Collectors.summingInt(e -> e.getStock())     // value: stock 합계
                ))
                .entrySet().stream()
                .map(entry -> new CategoryInventoryResponse(         // dto 변환
                        entry.getKey().getCategoryId(),
                        entry.getKey().getLabel(),
                        entry.getValue()
                ))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<SubCategoryInventoryResponse> getSubCategoryInventory(Long categoryId) {
        List<Equipment> allEquipment = equipmentRepository.findAllWithCategoryAndSubCategory();

        return allEquipment.stream()
                .filter(e -> e.getSubCategory().getCategory().getCategoryId().equals(categoryId))
                .collect(Collectors.groupingBy(
                        e -> e.getSubCategory(),                      // key: SubCategory
                        Collectors.summingInt(e -> e.getStock())    // value: stock 합계
                ))
                .entrySet().stream()
                .map(entry -> new SubCategoryInventoryResponse(
                        entry.getKey().getSubCategoryId(),
                        entry.getKey().getLabel(),
                        entry.getValue()
                ))
                .toList();
    }

    private String calcChangeRate(int current, int previous) {
        if (previous == 0) {
            return current == 0 ? "0%" : "신규 발생";
        }
        double rate = ((double)(current - previous) / previous) * 100;
        return String.format("%.1f%%", rate);
    }
}
