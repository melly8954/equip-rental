package com.equip.equiprental.rental.repository;

import com.equip.equiprental.rental.domain.RentalItem;
import com.equip.equiprental.rental.domain.RentalItemOverdue;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RentalItemOverdueRepository extends JpaRepository<RentalItemOverdue, Integer> {
    Optional<RentalItemOverdue> findByRentalItem(RentalItem item);
}
