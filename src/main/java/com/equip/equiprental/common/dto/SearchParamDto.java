package com.equip.equiprental.common.dto;

import com.equip.equiprental.board.domain.BoardType;
import com.equip.equiprental.common.exception.CustomException;
import com.equip.equiprental.common.exception.ErrorType;
import com.equip.equiprental.equipment.domain.EquipmentStatus;
import com.equip.equiprental.notification.domain.NotificationStatus;
import com.equip.equiprental.rental.domain.RentalItemStatus;
import com.equip.equiprental.rental.domain.RentalStatus;
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

    private Long boardId;

    private NotificationStatus notificationStatus;

    public Pageable getPageable() {
        return PageRequest.of(page - 1, size, Sort.by("createdAt").descending());
    }
}