package com.equip.equiprental.rental.repository;

import com.equip.equiprental.rental.domain.RentalItem;
import com.equip.equiprental.rental.repository.dsl.RentalItemQRepo;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RentalItemRepository extends JpaRepository<RentalItem, Long>, RentalItemQRepo {
    RentalItem findFirstByEquipmentItem_EquipmentItemIdAndActualReturnDateIsNull(Long equipmentItemId);
}
