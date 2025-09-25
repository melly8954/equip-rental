package com.equip.equiprental.equipment.service.iface;

import com.equip.equiprental.equipment.dto.CategoryDto;
import com.equip.equiprental.equipment.dto.SubCategoryDto;

import java.util.List;

public interface CategoryService {
    List<CategoryDto> getCategories();
    List<SubCategoryDto> getSubCategories(Long categoryId);
}
