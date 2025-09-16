package com.equip.equiprental.rental.dto;

import com.equip.equiprental.common.exception.CustomException;
import com.equip.equiprental.common.exception.ErrorType;
import com.equip.equiprental.rental.domain.RentalStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class UpdateRentalStatusDto {
    private Long equipmentId;
    private String newStatus;
    private String rejectReason;

    public RentalStatus getRentalStatusEnum(){
        if(this.newStatus == null){
            throw new CustomException(ErrorType.INVALID_STATUS_REQUEST);
        }
        try{
            return RentalStatus.valueOf(this.newStatus);
        }catch(IllegalArgumentException e){
            throw new CustomException(ErrorType.INVALID_STATUS_REQUEST);
        }
    }
}
