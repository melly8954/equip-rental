package com.equip.equiprental.rental.domain;

import com.equip.equiprental.common.domain.BaseEntity;
import com.equip.equiprental.equipment.domain.Equipment;
import com.equip.equiprental.member.domain.Member;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(name="rental_tbl")
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Rental extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="rental_id")
    private Long rentalId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "equipment_id")
    private Equipment equipment;

    @Column(name="request_start_date")
    private LocalDate requestStartDate;

    @Column(name="request_end_date")
    private LocalDate requestEndDate;

    private Integer quantity;

    @Column(name="rental_reason")
    private String rentalReason;

    @Enumerated(EnumType.STRING)
    private RentalStatus status;

    @Column(name="reject_reason")
    private String rejectReason;
}
