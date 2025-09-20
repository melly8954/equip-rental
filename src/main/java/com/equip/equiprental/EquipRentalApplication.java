package com.equip.equiprental;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class EquipRentalApplication {

    public static void main(String[] args) {
        SpringApplication.run(EquipRentalApplication.class, args);
    }

}
