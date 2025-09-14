package com.equip.equiprental.rental.repository;

import com.equip.equiprental.rental.domain.Rental;
import com.equip.equiprental.rental.repository.dsl.RentalQRepo;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RentalRepository extends JpaRepository<Rental, Integer>, RentalQRepo {
}
