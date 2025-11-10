package com.equip.equiprental.rental.repository.dsl;

import com.equip.equiprental.equipment.domain.QCategory;
import com.equip.equiprental.equipment.domain.QSubCategory;
import com.equip.equiprental.filestorage.domain.QFileMeta;
import com.equip.equiprental.rental.domain.QRental;
import com.equip.equiprental.rental.domain.RentalStatus;
import com.equip.equiprental.rental.dto.AdminRentalDto;
import com.equip.equiprental.rental.dto.RentalFilter;
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
    public Page<AdminRentalDto> findAdminRentals(RentalFilter paramDto, Pageable pageable) {
        QRental r = QRental.rental;
        QSubCategory sc = QSubCategory.subCategory;
        QCategory c = QCategory.category;
        QFileMeta f = QFileMeta.fileMeta;

        // 조건
        BooleanBuilder builder = new BooleanBuilder();
        builder.and(r.status.eq(RentalStatus.PENDING));

        if (paramDto.getDepartmentId() != null) {
            builder.and(r.member.department.departmentId.eq(paramDto.getDepartmentId()));
        }

        if (paramDto.getMemberName() != null && !paramDto.getMemberName().isEmpty()) {
            builder.and(r.member.name.containsIgnoreCase(paramDto.getMemberName()));
        }

        if (paramDto.getCategoryId() != null) {
            builder.and(c.categoryId.eq(paramDto.getCategoryId()));
        }

        if (paramDto.getSubCategoryId() != null) {
            builder.and(sc.subCategoryId.eq(paramDto.getSubCategoryId()));
        }

        // 기본 쿼리
        List<AdminRentalDto> content = queryFactory
                .select(Projections.constructor(AdminRentalDto.class,
                        r.rentalId,
                        r.equipment.equipmentId,
                        f.filePath,
                        r.quantity,
                        r.requestStartDate,
                        r.requestEndDate,
                        r.rentalReason,
                        r.createdAt,
                        r.member.memberId,
                        r.member.name,
                        r.member.department.departmentName,
                        c.label,
                        sc.label,
                        r.equipment.model
                ))
                .from(r)
                .leftJoin(r.equipment.subCategory, sc)
                .leftJoin(sc.category, c)
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
                .leftJoin(r.equipment.subCategory, sc)
                .leftJoin(sc.category, c)
                .where(builder)
                .fetchOne();
        total = (total != null) ? total : 0L;

        return new PageImpl<>(content, pageable, total);
    }

    @Override
    public Page<UserRentalDto> findUserRentals(RentalFilter paramDto, Pageable pageable, Long currentUserId) {
        QRental r = QRental.rental;
        QSubCategory sc = QSubCategory.subCategory;
        QCategory c = QCategory.category;
        QFileMeta f = QFileMeta.fileMeta;

        BooleanBuilder builder = new BooleanBuilder();
        builder.and(r.member.memberId.eq(currentUserId));

        if (paramDto.getCategoryId() != null) {
            builder.and(c.categoryId.eq(paramDto.getCategoryId()));
        }

        if (paramDto.getSubCategoryId() != null) {
            builder.and(sc.subCategoryId.eq(paramDto.getSubCategoryId()));
        }

        if (paramDto.getStatus() != null) {
            builder.and(r.status.eq(paramDto.getStatus()));
        }

        List<UserRentalDto> content = queryFactory
                .select(Projections.constructor(UserRentalDto.class,
                        r.rentalId,
                        r.equipment.equipmentId,
                        r.equipment.model,
                        c.label,
                        sc.label,
                        f.filePath,
                        r.requestStartDate,
                        r.requestEndDate,
                        r.quantity,
                        r.status.stringValue(),
                        r.rejectReason,
                        r.createdAt
                ))
                .from(r)
                .leftJoin(r.equipment.subCategory, sc)
                .leftJoin(sc.category, c)
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
                .leftJoin(r.equipment.subCategory, sc)
                .leftJoin(sc.category, c)
                .where(builder)
                .fetchOne();
        total = (total != null) ? total : 0L;

        return new PageImpl<>(content, pageable, total);
    }
}
