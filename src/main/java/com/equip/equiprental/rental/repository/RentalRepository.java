package com.equip.equiprental.rental.repository;

import com.equip.equiprental.rental.domain.Rental;
import com.equip.equiprental.rental.repository.dsl.RentalQRepo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;

public interface RentalRepository extends JpaRepository<Rental, Long>, RentalQRepo {
    @Query("""
        SELECT COUNT(r)
        FROM Rental r
        WHERE r.createdAt >= :start
          AND r.createdAt < :end
    """)
    int countThisMonth(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("""
        SELECT COUNT(r)
        FROM Rental r
        WHERE r.createdAt >= :start
          AND r.createdAt < :end
    """)
    int countLastMonth(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("""
        SELECT COUNT(r)
        FROM Rental r
        WHERE r.approvedAt >= :start
            AND r.approvedAt < :end
    """)
    int countApprovedThisMonth(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("""
        SELECT COUNT(r)
        FROM Rental r
        WHERE r.approvedAt >= :start
            AND r.approvedAt < :end
    """)
    int countApprovedLastMonth(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("""
        SELECT COUNT(r)
        FROM Rental r
        WHERE r.status = 'PENDING'
    """)
    int countPendingNow();
}
