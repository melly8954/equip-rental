package com.equip.equiprental.rental.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class ExtendRequestDto {
    private int days;
}
