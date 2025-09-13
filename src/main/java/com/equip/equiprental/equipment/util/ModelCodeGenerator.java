package com.equip.equiprental.equipment.util;

import java.util.Random;

public class ModelCodeGenerator {
    private static final String LETTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private final Random random;

    public ModelCodeGenerator(Random random) {
        this.random = random;
    }

    public String generate(String category, String subCategory) {
        String categoryCode = EquipmentCodeMap.categoryCodeMap.get(category);
        String subCategoryCode = EquipmentCodeMap.subCategoryCodeMap.get(subCategory);

        if (categoryCode == null || subCategoryCode == null) {
            throw new IllegalArgumentException("Invalid category or subCategory");
        }

        // 3자리 난수 생성
        int number = random.nextInt(1000); // 0 ~ 999
        String numberCode = String.format("%03d", number); // 3자리로 포맷

        return categoryCode + "-" + subCategoryCode + "-" + numberCode;
    }
}
