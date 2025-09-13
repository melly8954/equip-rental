package com.equip.equiprental.equipment.dto;

import com.equip.equiprental.common.exception.CustomException;
import com.equip.equiprental.common.exception.ErrorType;
import com.equip.equiprental.equipment.domain.EquipmentCategory;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class EquipmentRegisterRequest {
    private String category;
    private String subCategory;
    private String model;
    private Integer stock;

    public EquipmentCategory getCategoryEnum(){
        if(this.category == null){
            throw new CustomException(ErrorType.INVALID_EQUIP_CATEGORY_REQUEST);
        }
        try{
            return EquipmentCategory.valueOf(this.category);
        }catch(IllegalArgumentException e){
            throw new CustomException(ErrorType.INVALID_EQUIP_CATEGORY_REQUEST);
        }
    }
}
