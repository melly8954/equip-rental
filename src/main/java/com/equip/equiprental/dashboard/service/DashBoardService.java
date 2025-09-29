package com.equip.equiprental.dashboard.service;

import com.equip.equiprental.common.dto.PageResponseDto;
import com.equip.equiprental.common.dto.SearchParamDto;
import com.equip.equiprental.dashboard.dto.CategoryInventoryResponse;
import com.equip.equiprental.dashboard.dto.KpiResponseDto;
import com.equip.equiprental.dashboard.dto.SubCategoryInventoryResponse;
import com.equip.equiprental.dashboard.dto.ZeroStockDto;

import java.util.List;

public interface DashBoardService {
    KpiResponseDto getDashBoardKpi();

    PageResponseDto<ZeroStockDto> getDashBoardZeroStock(SearchParamDto paramDto);

    List<CategoryInventoryResponse> getCategoryInventory();
    List<SubCategoryInventoryResponse> getSubCategoryInventory(Long categoryId);
}
