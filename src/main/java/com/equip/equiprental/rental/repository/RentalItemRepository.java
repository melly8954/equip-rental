package com.equip.equiprental.rental.repository;

import com.equip.equiprental.rental.domain.RentalItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RentalItemRepository extends JpaRepository<RentalItem, Integer> {
}
