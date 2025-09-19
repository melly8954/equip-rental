package com.equip.equiprental.equipment.domain;

import com.equip.equiprental.common.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name="sub_category_tbl")
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SubCategory extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="sub_category_id")
    private Long subCategoryId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="category_id")
    private Category category;

    private String label;
}
