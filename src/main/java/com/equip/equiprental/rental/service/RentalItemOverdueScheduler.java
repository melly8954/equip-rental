package com.equip.equiprental.rental.service;

import com.equip.equiprental.rental.service.iface.RentalItemService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class RentalItemOverdueScheduler {
    private final RentalItemService rentalItemService;

    // 매일 자정 00:00에 실행
    @Scheduled(cron = "0 0 0 * * ?")
    public void markOverdue() {
        int updatedCount = rentalItemService.updateOverdueStatus();
        log.info("Overdue 상태로 업데이트된 RentalItem 수: " + updatedCount);
    }
}
