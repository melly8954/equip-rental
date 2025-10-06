package com.equip.equiprental.notification.dto;

import com.equip.equiprental.common.dto.SearchParamDto;
import com.equip.equiprental.notification.domain.NotificationStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class NotificationFilter extends SearchParamDto {
    private NotificationStatus status;
}
