package com.equip.equiprental.equipment.dto;

import com.equip.equiprental.common.exception.CustomException;
import com.equip.equiprental.common.exception.ErrorType;
import com.equip.equiprental.equipment.domain.EquipmentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class UpdateItemStatusDto {
    private Long equipmentItemId;
    private String newStatus;

    public EquipmentStatus getEquipmentItemStatusEnum(){
        if(this.newStatus == null){
            throw new CustomException(ErrorType.INVALID_STATUS_REQUEST);
        }
        try{
            return EquipmentStatus.valueOf(this.newStatus);
        }catch(IllegalArgumentException e){
            throw new CustomException(ErrorType.INVALID_STATUS_REQUEST);
        }
    }
}
