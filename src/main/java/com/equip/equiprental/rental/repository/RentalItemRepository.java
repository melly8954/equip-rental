package com.equip.equiprental.rental.repository;

import com.equip.equiprental.rental.domain.RentalItem;
import com.equip.equiprental.rental.domain.RentalItemStatus;
import com.equip.equiprental.rental.repository.dsl.RentalItemQRepo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;

public interface RentalItemRepository extends JpaRepository<RentalItem, Long>, RentalItemQRepo {
    List<RentalItem> findByStatusAndEndDateBefore(RentalItemStatus rentalItemStatus, LocalDate now);

    List<RentalItem> findByRental_RentalId(Long rentalId);
}
