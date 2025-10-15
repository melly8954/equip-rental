package com.equip.equiprental.dashboard.service;

import com.equip.equiprental.common.dto.PageResponseDto;
import com.equip.equiprental.common.dto.SearchParamDto;
import com.equip.equiprental.dashboard.dto.*;

import java.util.List;

public interface DashBoardService {
    KpiResponseDto getDashBoardKpi();

    PageResponseDto<ZeroStockDto> getDashBoardZeroStock(SearchParamDto paramDto);

    List<CategoryInventoryResponse> getCategoryInventory();
    List<SubCategoryInventoryResponse> getSubCategoryInventory(Long categoryId);

    PageResponseDto<InventoryDetail> getInventoryDetail(Long subCategoryId, SearchParamDto paramDto);
}
