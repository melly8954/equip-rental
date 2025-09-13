package com.equip.equiprental.equipment.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@AllArgsConstructor
@Builder
public class EquipmentRegisterResponse {
    private Long equipmentId;
    private String category;
    private String subCategory;
    private String model;
    private int stock;
    private List<EquipmentImage> images;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @Getter
    @AllArgsConstructor
    @Builder
    public static class EquipmentImage {
        private Long fileId;
        private String originalName;
        private String url;
        private int fileOrder;
        private String fileType;
        private long fileSize;
    }
}
