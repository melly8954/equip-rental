package com.equip.equiprental.equipment.service;

import com.equip.equiprental.equipment.domain.Category;
import com.equip.equiprental.equipment.domain.SubCategory;
import com.equip.equiprental.equipment.dto.CategoryDto;
import com.equip.equiprental.equipment.dto.SubCategoryDto;
import com.equip.equiprental.equipment.repository.CategoryRepository;
import com.equip.equiprental.equipment.repository.SubCategoryRepository;
import com.equip.equiprental.equipment.service.iface.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {
    private final CategoryRepository categoryRepository;
    private final SubCategoryRepository subCategoryRepository;

    @Override
    public List<CategoryDto> getCategories() {
        List<Category> categories = categoryRepository.findAll();

        return categories.stream()
                .map(c -> CategoryDto.builder()
                        .categoryId(c.getCategoryId())
                        .categoryCode(c.getCategoryCode())
                        .label(c.getLabel())
                        .build())
                .toList();
    }

    @Override
    public List<SubCategoryDto> getSubCategories(Long categoryId) {
        List<SubCategory> subCategories = subCategoryRepository.findByCategoryCategoryId(categoryId);

        return subCategories.stream()
                .map(sc -> SubCategoryDto.builder()
                        .subCategoryId(sc.getSubCategoryId())
                        .categoryId(sc.getCategory().getCategoryId())
                        .label(sc.getLabel())
                        .build())
                .toList();
    }
}
