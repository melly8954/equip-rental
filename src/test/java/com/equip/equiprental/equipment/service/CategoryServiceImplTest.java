package com.equip.equiprental.equipment.service;

import com.equip.equiprental.equipment.domain.Category;
import com.equip.equiprental.equipment.domain.SubCategory;
import com.equip.equiprental.equipment.dto.CategoryDto;
import com.equip.equiprental.equipment.dto.SubCategoryDto;
import com.equip.equiprental.equipment.repository.CategoryRepository;
import com.equip.equiprental.equipment.repository.SubCategoryRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("CategoryServiceImpl 단위 테스트")
public class CategoryServiceImplTest {
    @Mock private  CategoryRepository categoryRepository;
    @Mock private  SubCategoryRepository subCategoryRepository;

    @InjectMocks
    private CategoryServiceImpl categoryService;

    @Nested
    @DisplayName("getCategories 메서드 테스트")
    class GetCategoriesTest {
        @Test
        @DisplayName("성공 - 모든 카테고리 조회 후 DTO 반환")
        void getCategories_shouldReturnCategoryDtoList() {
            // given
            Category category = Category.builder()
                    .categoryId(1L)
                    .categoryCode("CAT001")
                    .label("전자기기")
                    .build();
            when(categoryRepository.findAll()).thenReturn(List.of(category));

            // when
            List<CategoryDto> result = categoryService.getCategories();

            // then
            assertThat(result).hasSize(1);
            CategoryDto dto = result.get(0);
            assertThat(dto.getCategoryId()).isEqualTo(category.getCategoryId());
            assertThat(dto.getCategoryCode()).isEqualTo(category.getCategoryCode());
            assertThat(dto.getLabel()).isEqualTo(category.getLabel());
        }
    }

    @Nested
    @DisplayName("getSubCategories 메서드 테스트")
    class GetSubCategoriesTest {
        @Test
        @DisplayName("성공 - 특정 카테고리의 서브카테고리 조회 후 DTO 반환")
        void getSubCategories_shouldReturnSubCategoryDtoList() {
            // given
            Category category = Category.builder()
                    .categoryId(1L)
                    .categoryCode("CAT001")
                    .label("전자기기")
                    .build();

            SubCategory subCategory = SubCategory.builder()
                    .subCategoryId(10L)
                    .category(category)
                    .label("노트북")
                    .build();

            when(subCategoryRepository.findByCategoryCategoryId(1L))
                    .thenReturn(List.of(subCategory));

            // when
            List<SubCategoryDto> result = categoryService.getSubCategories(1L);

            // then
            assertThat(result).hasSize(1);
            SubCategoryDto dto = result.get(0);
            assertThat(dto.getSubCategoryId()).isEqualTo(subCategory.getSubCategoryId());
            assertThat(dto.getCategoryId()).isEqualTo(category.getCategoryId());
            assertThat(dto.getLabel()).isEqualTo(subCategory.getLabel());
        }
    }
}
