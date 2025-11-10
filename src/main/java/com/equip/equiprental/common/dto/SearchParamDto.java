package com.equip.equiprental.common.dto;

import lombok.*;
import lombok.experimental.SuperBuilder;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class SearchParamDto {
    @Builder.Default
    private int page = 1;
    @Builder.Default
    private int size = 10;

    public Pageable getPageable() {
        return PageRequest.of(page - 1, size, Sort.by("createdAt").descending());
    }
}