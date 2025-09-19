package com.equip.equiprental.rental.repository.dsl;

import com.equip.equiprental.common.dto.SearchParamDto;
import com.equip.equiprental.filestorage.domain.QFileMeta;
import com.equip.equiprental.rental.domain.QRental;
import com.equip.equiprental.rental.domain.RentalStatus;
import com.equip.equiprental.rental.dto.AdminRentalDto;
import com.equip.equiprental.rental.dto.UserRentalDto;
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
public class RentalQRepoImpl implements RentalQRepo{

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<AdminRentalDto> findAdminRentals(SearchParamDto paramDto, Pageable pageable) {
        QRental r = QRental.rental;

        BooleanBuilder builder = new BooleanBuilder();
        builder.and(r.status.eq(RentalStatus.PENDING));

        if (paramDto.getDepartment() != null && !paramDto.getDepartment().isEmpty()) {
            builder.and(r.member.department.departmentName.eq(paramDto.getDepartment()));
        }

        if (paramDto.getMemberName() != null && !paramDto.getMemberName().isEmpty()) {
            builder.and(r.member.name.containsIgnoreCase(paramDto.getMemberName()));
        }

        if (paramDto.getCategoryEnum() != null) {
            builder.and(r.equipment.category.eq(paramDto.getCategoryEnum()));
        }

        if (paramDto.getSubCategory() != null && !paramDto.getSubCategory().isEmpty()) {
            builder.and(r.equipment.subCategory.eq(paramDto.getSubCategory()));
        }

        List<AdminRentalDto> content = queryFactory
                .select(Projections.constructor(AdminRentalDto.class,
                        r.rentalId,
                        r.equipment.equipmentId,
                        r.quantity,
                        r.requestStartDate,
                        r.requestEndDate,
                        r.rentalReason,
                        r.createdAt,
                        r.member.memberId,
                        r.member.name,
                        r.member.department,
                        r.equipment.category.stringValue(),
                        r.equipment.subCategory,
                        r.equipment.model
                ))
                .from(r)
                .where(builder)
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .orderBy(r.createdAt.desc())
                .fetch();

        // total count
        Long total = queryFactory
                .select(r.count())
                .from(r)
                .where(builder)
                .fetchOne();
        total = (total != null) ? total : 0L;

        return new PageImpl<>(content, pageable, total);
    }

    @Override
    public Page<UserRentalDto> findUserRentals(SearchParamDto paramDto, Pageable pageable, Long memberId) {
        QRental r = QRental.rental;
        QFileMeta f = QFileMeta.fileMeta;

        BooleanBuilder builder = new BooleanBuilder();
        builder.and(r.member.memberId.eq(memberId));

        if (paramDto.getCategoryEnum() != null) {
            builder.and(r.equipment.category.eq(paramDto.getCategoryEnum()));
        }

        if (paramDto.getSubCategory() != null && !paramDto.getSubCategory().isEmpty()) {
            builder.and(r.equipment.subCategory.eq(paramDto.getSubCategory()));
        }

        if (paramDto.getRentalStatus() != null && !paramDto.getRentalStatus().isEmpty()) {
            builder.and(r.status.eq(paramDto.getRentalStatusEnum()));
        }

        List<UserRentalDto> content = queryFactory
                .select(Projections.constructor(UserRentalDto.class,
                        r.rentalId,
                        r.equipment.model,
                        r.equipment.category.stringValue(),
                        r.equipment.subCategory,
                        f.filePath,
                        r.requestStartDate,
                        r.requestEndDate,
                        r.quantity,
                        r.status.stringValue(),
                        r.rejectReason
                ))
                .from(r)
                .leftJoin(f).on(f.relatedType.eq("equipment").and(f.relatedId.eq(r.equipment.equipmentId)))
                .where(builder)
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .orderBy(r.createdAt.desc())
                .fetch();

        // total count
        Long total = queryFactory
                .select(r.count())
                .from(r)
                .where(builder)
                .fetchOne();
        total = (total != null) ? total : 0L;

        return new PageImpl<>(content, pageable, total);
    }
}
