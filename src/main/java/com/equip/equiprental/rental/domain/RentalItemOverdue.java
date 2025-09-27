package com.equip.equiprental.rental.domain;

import com.equip.equiprental.common.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(name="rental_item_overdue_tbl")
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RentalItemOverdue extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "overdue_id")
    private Long overdueId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rental_item_id")
    private RentalItem rentalItem;

    @Column(name = "overdue_days")
    private Integer overdueDays;

    @Column(name = "planned_end_date")
    private LocalDate plannedEndDate;

    @Column(name = "actual_return_date")
    private LocalDate actualReturnDate;
}
