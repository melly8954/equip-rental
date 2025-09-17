package com.equip.equiprental.rental.repository.dsl;

import com.equip.equiprental.common.dto.SearchParamDto;
import com.equip.equiprental.filestorage.domain.QFileMeta;
import com.equip.equiprental.rental.domain.QRentalItem;
import com.equip.equiprental.rental.dto.AdminRentalItemDto;
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
public class RentalItemQRepoImpl implements RentalItemQRepo{

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<AdminRentalItemDto> findAdminRentalItems(SearchParamDto paramDto, Pageable pageable) {
        QRentalItem i = QRentalItem.rentalItem;
        QFileMeta f = QFileMeta.fileMeta;

        BooleanBuilder builder = new BooleanBuilder();

        if (paramDto.getDepartment() != null && !paramDto.getDepartment().isEmpty()) {
            builder.and(i.rental.member.department.eq(paramDto.getDepartment()));
        }

        if (paramDto.getMemberName() != null && !paramDto.getMemberName().isEmpty()) {
            builder.and(i.rental.member.name.containsIgnoreCase(paramDto.getMemberName()));
        }

        if (paramDto.getCategoryEnum() != null) {
            builder.and(i.rental.equipment.category.eq(paramDto.getCategoryEnum()));
        }

        if (paramDto.getSubCategory() != null && !paramDto.getSubCategory().isEmpty()) {
            builder.and(i.rental.equipment.subCategory.eq(paramDto.getSubCategory()));
        }

        List<AdminRentalItemDto> results = queryFactory
                .select(Projections.constructor(AdminRentalItemDto.class,
                        i.rentalItemId,
                        i.rental.rentalId,
                        f.filePath,
                        i.rental.equipment.category.stringValue(),
                        i.rental.equipment.subCategory,
                        i.rental.equipment.model,
                        i.equipmentItem.serialNumber,
                        i.rental.member.name,
                        i.rental.member.department,
                        i.startDate,
                        i.endDate,
                        i.actualReturnDate,
                        Expressions.constant(false),
                        i.isExtended
                ))
                .from(i)
                .leftJoin(f).on(f.relatedType.eq("equipment").and(f.relatedId.eq(i.rental.equipment.equipmentId)))
                .where(builder)
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .orderBy(i.createdAt.desc(), i.rentalItemId.desc())
                .fetch();

        // 카운트 조회
        Long total = queryFactory
                .select(i.count())
                .from(i)
                .where(builder)
                .fetchOne();
        total = total == null ? 0 : total;

        return new PageImpl<>(results, pageable, total);
    }
}
