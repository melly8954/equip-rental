package com.equip.equiprental.equipment.repository.dsl;

import com.equip.equiprental.dashboard.dto.InventoryDetail;
import com.equip.equiprental.equipment.domain.EquipmentStatus;
import com.equip.equiprental.equipment.domain.QEquipment;
import com.equip.equiprental.equipment.domain.QEquipmentItem;
import com.equip.equiprental.equipment.dto.EquipmentDto;
import com.equip.equiprental.equipment.dto.EquipmentFilter;
import com.equip.equiprental.filestorage.domain.QFileMeta;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class EquipmentQRepoImpl implements EquipmentQRepo {

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<EquipmentDto> findByFilters(EquipmentFilter paramDto, Pageable pageable) {
        QEquipment e = QEquipment.equipment;
        QEquipmentItem i = QEquipmentItem.equipmentItem;
        QFileMeta f = QFileMeta.fileMeta;

        // 동적 조건을 누적할 BooleanBuilder 생성
        BooleanBuilder builder = new BooleanBuilder();

        builder.and(e.deleted.eq(false));

        if (paramDto.getCategoryId() != null) {
            builder.and(e.subCategory.category.categoryId.eq(paramDto.getCategoryId()));
        }
        if (paramDto.getSubCategoryId() != null) {
            builder.and(e.subCategory.subCategoryId.eq(paramDto.getSubCategoryId()));
        }
        if (paramDto.getModel() != null && !paramDto.getModel().isEmpty()) {
            builder.and(e.model.containsIgnoreCase(paramDto.getModel()));
        }

        // 조회용 DTO(EquipmentDto) 응답 객체 반환, DTO Projection
        List<EquipmentDto> content = queryFactory
                // 메인 쿼리, EquipmentDto 생성자의 파라미터 순서에 맞춰 값 세팅
                .select(Projections.constructor(EquipmentDto.class,
                        e.equipmentId,
                        e.subCategory.category.label,
                        e.subCategory.label,
                        e.model,
                        // available 재고, 서브 쿼리 (JPAExpressions.select)
                        // .count()는 NumberExpression<Long> 반환 -> 타입 변환
                        JPAExpressions.select(i.count().castToNum(Integer.class))
                                .from(i)
                                .where(i.equipment.eq(e)
                                        .and(i.status.eq(EquipmentStatus.AVAILABLE))),
                        // 전체 재고
                        JPAExpressions.select(i.count().castToNum(Integer.class))
                                .from(i)
                                .where(i.equipment.eq(e)),
                        f.filePath
                ))
                .from(e)
                .leftJoin(f).on(f.relatedType.eq("equipment").and(f.relatedId.eq(e.equipmentId)))
                .where(builder)
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .orderBy(e.equipmentId.desc())
                .fetch();

        // total count
        Long total = queryFactory
                .select(e.count())
                .from(e)
                .where(builder)
                .fetchOne();
        total = (total != null) ? total : 0L;

        return new PageImpl<>(content, pageable, total);
    }

    @Override
    public Page<InventoryDetail> findInventoryDetail(Long subCategoryId, Pageable pageable) {
        QEquipment e = QEquipment.equipment;
        QEquipmentItem i = QEquipmentItem.equipmentItem;

        // 동적 조건을 누적할 BooleanBuilder 생성
        BooleanBuilder builder = new BooleanBuilder();

        builder.and(e.deleted.eq(false));
        builder.and(e.subCategory.subCategoryId.eq(subCategoryId));

        // ✅ 메인 쿼리
        List<InventoryDetail> contents = queryFactory
                .select(Projections.constructor(
                        InventoryDetail.class,
                        e.equipmentId,
                        e.model,
                        e.stock,
                        new CaseBuilder()
                                .when(i.status.eq(EquipmentStatus.AVAILABLE)).then(1)
                                .otherwise(0)
                                .sum()
                                .as("availableCount")
                ))
                .from(e)
                .leftJoin(e.items, i)
                .where(builder)
                .groupBy(e.equipmentId, e.model, e.stock)
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        Long total = queryFactory
                .select(e.count())
                .from(e)
                .where(
                        e.subCategory.subCategoryId.eq(subCategoryId),
                        e.deleted.isFalse()
                )
                .fetchOne();
        total = (total != null) ? total : 0L;

        return new PageImpl<>(contents, pageable, total);
    }
}
