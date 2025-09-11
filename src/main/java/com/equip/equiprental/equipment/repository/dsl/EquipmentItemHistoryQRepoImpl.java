package com.equip.equiprental.equipment.repository.dsl;

import com.equip.equiprental.equipment.domain.EquipmentItemHistory;
import com.equip.equiprental.equipment.domain.QEquipmentItem;
import com.equip.equiprental.equipment.domain.QEquipmentItemHistory;
import com.equip.equiprental.member.domain.QMember;
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
    public Page<EquipmentItemHistory> findByEquipmentItemIdWithMember(Long equipmentItemId, Pageable pageable) {
        QEquipmentItemHistory history = QEquipmentItemHistory.equipmentItemHistory;
        QMember member = QMember.member;
        QEquipmentItem item = QEquipmentItem.equipmentItem;

        List<EquipmentItemHistory> content = queryFactory
                .selectFrom(history)
                .join(history.item, item).fetchJoin()
                .join(history.changedBy, member).fetchJoin()
                .where(item.equipmentItemId.eq(equipmentItemId))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .orderBy(history.createdAt.desc())
                .fetch();

        Long total = queryFactory
                .select(history.count())
                .from(history)
                .where(history.item.equipmentItemId.eq(equipmentItemId))
                .fetchOne();
        total = (total != null) ? total : 0L;

        return new PageImpl<>(content, pageable, total);
    }
}
