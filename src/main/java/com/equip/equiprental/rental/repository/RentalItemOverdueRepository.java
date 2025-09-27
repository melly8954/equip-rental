package com.equip.equiprental.rental.repository;

import com.equip.equiprental.rental.domain.RentalItemOverdue;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RentalItemOverdueRepository extends JpaRepository<RentalItemOverdue, Integer> {
}
