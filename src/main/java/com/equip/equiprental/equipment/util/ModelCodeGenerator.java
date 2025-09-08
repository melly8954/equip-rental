package com.equip.equiprental.equipment.util;

import java.util.Random;

public class ModelCodeGenerator {
    private static final String LETTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private final Random random;

    public ModelCodeGenerator(Random random) {
        this.random = random;
    }

    public String generate() {
        StringBuilder sb = new StringBuilder();

        // 영문 3글자
        for (int i = 0; i < 3; i++) {
            sb.append(LETTERS.charAt(random.nextInt(LETTERS.length())));
        }

        // 숫자 2글자
        for (int i = 0; i < 2; i++) {
            sb.append(random.nextInt(10)); // 0~9
        }

        return sb.toString();
    }
}
