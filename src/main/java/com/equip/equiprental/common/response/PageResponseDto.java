package com.equip.equiprental.common.response;

import lombok.*;

import java.util.List;


@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PageResponseDto<T> {
    private List<T> content;
    private int page;              // 현재 페이지 번호, 1부터 시작 (1-based)
    private int size;              // 페이지당 항목 수
    private long totalElements;    // 전체 항목 수
    private int totalPages;        // 전체 페이지 수
    private int numberOfElements;  // 현재 페이지 항목 수
    private boolean first;         // 첫 페이지 여부
    private boolean last;          // 마지막 페이지 여부
    private boolean empty;         // 현재 페이지가 비어 있는지
}
