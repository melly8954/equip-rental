package com.equip.equiprental.equipment.repository.dsl;

import com.equip.equiprental.equipment.domain.QEquipmentItemHistory;
import com.equip.equiprental.equipment.dto.EquipmentItemHistoryDto;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class EquipmentItemHistoryQRepoImpl implements EquipmentItemHistoryQRepo {

    private final JPAQueryFactory queryFactory;


    @Override
    public Page<EquipmentItemHistoryDto> findHistoriesByEquipmentItemId(Long equipmentItemId, Pageable pageable) {
        QEquipmentItemHistory h = QEquipmentItemHistory.equipmentItemHistory;

        BooleanBuilder builder = new BooleanBuilder();
        if(equipmentItemId != null) {
            builder.and(h.item.equipmentItemId.eq(equipmentItemId));
        }

        List<EquipmentItemHistoryDto> content = queryFactory
                .select(Projections.constructor(EquipmentItemHistoryDto.class,
                        h.item.equipmentItemId,
                        h.oldStatus.stringValue(),
                        h.newStatus.stringValue(),
                        h.changedBy.name,
                        Expressions.constant(""),
                        Expressions.constant(""),
                        h.createdAt
                ))
                .from(h)
                .where(builder)
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .orderBy(h.createdAt.desc())
                .fetch();

        Long total = queryFactory
                .select(h.count())
                .from(h)
                .where(builder)
                .fetchOne();
        total = total == null ? 0 : total;

        return new PageImpl<>(content, pageable, total);
    }
}
