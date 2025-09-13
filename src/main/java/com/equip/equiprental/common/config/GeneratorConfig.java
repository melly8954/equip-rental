package com.equip.equiprental.common.config;

import com.equip.equiprental.equipment.util.ModelCodeGenerator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Random;

@Configuration
public class GeneratorConfig {
    @Bean
    public ModelCodeGenerator modelCodeGenerator() {
        return new ModelCodeGenerator(new Random());
    }
}
