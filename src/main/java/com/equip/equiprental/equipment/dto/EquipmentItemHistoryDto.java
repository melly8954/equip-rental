package com.equip.equiprental.equipment.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@Builder
public class EquipmentItemHistoryDto {
    private Long equipmentItemId;
    private String oldStatus;
    private String newStatus;
    private String changedBy;

    private String currentOwnerName;   // ex. "홍길동" or "관리자"
    private String currentOwnerDept;   // ex. "개발팀" or "시스템"

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
}
