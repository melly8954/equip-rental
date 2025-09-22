package com.equip.equiprental.rental.repository;

import com.equip.equiprental.rental.domain.RentalItem;
import com.equip.equiprental.rental.repository.dsl.RentalItemQRepo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface RentalItemRepository extends JpaRepository<RentalItem, Long>, RentalItemQRepo {
    RentalItem findFirstByEquipmentItem_EquipmentItemIdAndActualReturnDateIsNull(Long equipmentItemId);

    @Modifying
    @Query("UPDATE RentalItem r SET r.status = 'OVERDUE' " +
            "WHERE r.status = 'RENTED' AND r.actualReturnDate IS NULL AND r.endDate < CURRENT_DATE")
    int markOverdueRentalItems();

    List<RentalItem> findByRental_RentalId(Long rentalId);
}
