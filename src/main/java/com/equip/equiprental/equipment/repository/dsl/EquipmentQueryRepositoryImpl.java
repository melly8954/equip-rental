package com.equip.equiprental.equipment.repository.dsl;

import com.equip.equiprental.common.dto.SearchParamDto;
import com.equip.equiprental.equipment.domain.EquipmentStatus;
import com.equip.equiprental.equipment.domain.QEquipment;
import com.equip.equiprental.equipment.domain.QEquipmentItem;
import com.equip.equiprental.equipment.dto.EquipmentDto;
import com.equip.equiprental.filestorage.repository.FileRepository;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class EquipmentQueryRepositoryImpl implements EquipmentQueryRepository{

    private final JPAQueryFactory queryFactory;
    private final FileRepository fileRepository;

    @Override
    public Page<EquipmentDto> findByFilters(SearchParamDto paramDto, Pageable pageable) {
        QEquipment e = QEquipment.equipment;
        QEquipmentItem i = QEquipmentItem.equipmentItem;

        // 동적 조건을 누적할 BooleanBuilder 생성
        BooleanBuilder builder = new BooleanBuilder();
        if (paramDto.getCategoryEnum() != null) {
            builder.and(e.category.eq(paramDto.getCategoryEnum()));
        }
        if (paramDto.getSubCategory() != null && !paramDto.getSubCategory().isEmpty()) {
            builder.and(e.subCategory.eq(paramDto.getSubCategory()));
        }
        if (paramDto.getModel() != null && !paramDto.getModel().isEmpty()) {
            builder.and(e.model.containsIgnoreCase(paramDto.getModel()));
        }

        // 페이지 조회
        List<EquipmentDto> content = queryFactory
                .select(Projections.constructor(EquipmentDto.class,
                        e.equipmentId,
                        e.category.stringValue(),
                        e.subCategory,
                        e.model,
                        JPAExpressions.select(i.count().castToNum(Integer.class))
                                .from(i)
                                .where(i.equipment.eq(e)
                                        .and(i.status.eq(EquipmentStatus.AVAILABLE))),
                        Expressions.asString("") // imageUrl는 나중에 처리
                ))
                .from(e)
                .where(builder)
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .orderBy(e.equipmentId.desc())
                .fetch();

        // imageUrl 채우기
        content = content.stream()
                .map(dto -> EquipmentDto.builder()
                        .equipmentId(dto.getEquipmentId())
                        .category(dto.getCategory())
                        .subCategory(dto.getSubCategory())
                        .model(dto.getModel())
                        .stock(dto.getStock())
                        .imageUrl(fileRepository.findUrlsByEquipmentId(dto.getEquipmentId())
                                .stream().findFirst().orElse(null))
                        .build())
                .collect(Collectors.toList());

        // total count
        Long total = queryFactory
                .select(e.count())
                .from(e)
                .where(builder)
                .fetchOne();
        total = (total != null) ? total : 0L;

        return new PageImpl<>(content, pageable, total);
    }
}
