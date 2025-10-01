package com.equip.equiprental.rental.service;

import com.equip.equiprental.notification.domain.NotificationType;
import com.equip.equiprental.notification.service.iface.NotificationService;
import com.equip.equiprental.rental.domain.RentalItem;
import com.equip.equiprental.rental.repository.RentalItemRepository;
import com.equip.equiprental.rental.service.iface.RentalItemService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class RentalItemOverdueScheduler {
    private final RentalItemService rentalItemService;
    private final RentalItemRepository rentalItemRepository;
    private final NotificationService notificationService;

    // 매일 자정 00:00에 실행
    @Scheduled(cron = "0 0 0 * * ?")
    public void markOverdue() {
        rentalItemService.updateOverdueStatus();
    }

    // 매일 자정 실행
    @Scheduled(cron = "0 0 0 * * ?")
    @Transactional
    public void sendDueNotifications() {
        LocalDate tomorrow = LocalDate.now().plusDays(1);

        List<RentalItem> dueItems = rentalItemRepository.findByEndDate(tomorrow);

        for (RentalItem item : dueItems) {
            notificationService.createNotification(
                    item.getRental().getMember(),
                    NotificationType.RENTAL_DUE_TOMORROW,
                    "'" + item.getEquipmentItem().getEquipment().getModel() + "' 장비 반납 예정일이 내일입니다.",
                    null
            );
        }
    }
}
