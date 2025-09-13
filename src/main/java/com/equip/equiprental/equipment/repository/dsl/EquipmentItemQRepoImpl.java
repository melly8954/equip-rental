package com.equip.equiprental.equipment.repository.dsl;

import com.equip.equiprental.equipment.domain.EquipmentStatus;
import com.equip.equiprental.equipment.domain.QEquipmentItem;
import com.equip.equiprental.equipment.dto.EquipmentItemDto;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class EquipmentItemQRepoImpl implements EquipmentItemQRepo {

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<EquipmentItemDto> findByStatus(Long equipmentId, EquipmentStatus status, Pageable pageable) {
        QEquipmentItem i = QEquipmentItem.equipmentItem;

        // 동적 조건을 누적할 BooleanBuilder 생성
        BooleanBuilder builder = new BooleanBuilder();

        builder.and(i.equipment.equipmentId.eq(equipmentId));
        
        if (status != null){
            builder.and(i.status.eq(status));
        }

        // 조회용 DTO(EquipmentDto) 응답 객체 반환, DTO Projection
        List<EquipmentItemDto> content = queryFactory
                // 메인 쿼리, EquipmentDto 생성자의 파라미터 순서에 맞춰 값 세팅
                .select(Projections.constructor(EquipmentItemDto.class,
                        i.equipmentItemId,
                        i.serialNumber,
                        i.status
                ))
                .from(i)
                .where(builder)
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .orderBy(i.equipmentItemId.desc())
                .fetch();

        // total count
        Long total = queryFactory
                .select(i.count())
                .from(i)
                .where(builder)
                .fetchOne();
        total = total == null ? 0 : total;

        return new PageImpl<>(content, pageable, total);
    }
}
