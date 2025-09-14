package com.equip.equiprental.rental.repository;

import com.equip.equiprental.rental.domain.Rental;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RentalRepository extends JpaRepository<Rental, Integer> {
}
