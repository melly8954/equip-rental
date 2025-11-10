package com.equip.equiprental.scope.domain;

import com.equip.equiprental.equipment.domain.Category;
import com.equip.equiprental.member.domain.Member;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name="manager_scope_tbl")
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ManagerScope {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="scope_id")
    private Long scopeId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="manager_id")
    private Member manager;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="category_id")
    private Category category;

    public void updateCategory(Category category) {
        this.category = category;
    }
}
