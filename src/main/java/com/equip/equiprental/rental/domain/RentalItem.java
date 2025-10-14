package com.equip.equiprental.rental.domain;

import com.equip.equiprental.common.domain.BaseEntity;
import com.equip.equiprental.common.exception.CustomException;
import com.equip.equiprental.common.exception.ErrorType;
import com.equip.equiprental.equipment.domain.EquipmentItem;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(name="rental_item_tbl")
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RentalItem extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="rental_item_id")
    private Long rentalItemId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="rental_id")
    private Rental rental;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="equipment_item_id")
    private EquipmentItem equipmentItem;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Enumerated(EnumType.STRING)
    private RentalItemStatus status;

    @Column(name = "actual_return_date")
    private LocalDate actualReturnDate;

    @Column(name = "is_extended")
    private Boolean isExtended;

    // 대여 연장
    public void extend(LocalDate newEndDate) {
        if (Boolean.TRUE.equals(this.isExtended)) {
            throw new CustomException(ErrorType.CONFLICT, "이미 연장된 대여건입니다.");
        }
        if (this.actualReturnDate != null) {
            throw new CustomException(ErrorType.CONFLICT, "이미 반납된 대여건은 연장할 수 없습니다.");
        }
        if (this.endDate.isBefore(LocalDate.now())) {
            throw new CustomException(ErrorType.CONFLICT, "이미 연체된 대여건은 연장할 수 없습니다.");
        }
        this.endDate = newEndDate;
        this.isExtended = true;
    }
    
    // 대여 실 반납일 지정
    public void returnItem(LocalDate now) {
        this.actualReturnDate = now;
        this.status = RentalItemStatus.RETURNED;
    }

    public void rentalOverdue(RentalItemStatus status) {
        this.status = status;
    }
}
