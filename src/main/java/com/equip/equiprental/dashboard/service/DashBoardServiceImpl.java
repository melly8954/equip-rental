package com.equip.equiprental.dashboard.service;

import com.equip.equiprental.common.dto.PageResponseDto;
import com.equip.equiprental.common.dto.SearchParamDto;
import com.equip.equiprental.dashboard.dto.*;
import com.equip.equiprental.equipment.domain.Category;
import com.equip.equiprental.equipment.domain.Equipment;
import com.equip.equiprental.equipment.domain.EquipmentStatus;
import com.equip.equiprental.equipment.domain.SubCategory;
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
import java.time.LocalTime;
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
        LocalDateTime firstDayThisMonth = now.withDayOfMonth(1).with(LocalTime.MIN);;
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

        // KPI 리스트 생성
        List<KpiItemDto> kpis = Arrays.asList(
                new KpiItemDto("이번 달 신규 대여 신청 수", newRequestsThisMonth, newRequestsChange),
                new KpiItemDto("이번 달 승인된 대여  신청 수", approvedThisMonth, approvedChange),
                new KpiItemDto("현재 승인 대기 중인 신청 수", pendingCount, null),
                new KpiItemDto("현재 대여 연체 중인 장비 수", overdueCount, null)
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
        Page<Equipment> page = equipmentRepository.findZeroAvailableStock(pageable);

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
        List<Equipment> allEquipment = equipmentRepository.findAllWithCategorySubCategoryAndItems();

        return allEquipment.stream()
                // 카테고리(Category)를 기준으로 장비(Equipment)를 그룹핑
                .collect(Collectors.groupingBy(e -> e.getSubCategory().getCategory()))
                .entrySet().stream()    // Map<Category, List<Equipment>> → Stream<Map.Entry<Category, List<Equipment>>>
                .map(entry -> {
                    Category category = entry.getKey();
                    List<Equipment> equipments = entry.getValue();

                    // 총 재고 합계
                    int totalStock = equipments.stream()
                            .mapToInt(Equipment::getStock)
                            .sum();

                    // 사용 가능한 재고 합계
                    int availableStock = equipments.stream()
                            .flatMap(e -> e.getItems().stream())
                            .filter(item -> item.getStatus() == EquipmentStatus.AVAILABLE)
                            .mapToInt(i -> 1)
                            .sum();

                    // DTO 생성
                    return CategoryInventoryResponse.builder()
                            .categoryId(category.getCategoryId())
                            .categoryLabel(category.getLabel())
                            .totalStock(totalStock)
                            .availableStock(availableStock)
                            .build();
                })
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<SubCategoryInventoryResponse> getSubCategoryInventory(Long categoryId) {
        List<Equipment> allEquipment = equipmentRepository.findAllWithCategorySubCategoryAndItems();

        return allEquipment.stream()
                .filter(e -> e.getSubCategory().getCategory().getCategoryId().equals(categoryId))
                // 카테고리 기준 장비 그룹핑 → Map<SubCategory, List<Equipment>>
                .collect(Collectors.groupingBy(Equipment::getSubCategory))
                // Map의 값 스트림으로 변환
                .entrySet().stream()
                .map(entry -> {
                    SubCategory sc = entry.getKey();
                    List<Equipment> equipments = entry.getValue();

                    int totalStock = equipments.stream()
                            .mapToInt(Equipment::getStock)
                            .sum();

                    int availableStock = equipments.stream()
                            .flatMap(e -> e.getItems().stream())
                            .filter(item -> item.getStatus() == EquipmentStatus.AVAILABLE)
                            .mapToInt(i -> 1)
                            .sum();

                    // DTO 생성
                    return new SubCategoryInventoryResponse(
                            sc.getSubCategoryId(),
                            sc.getLabel(),
                            totalStock,
                            availableStock
                    );
                })
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
