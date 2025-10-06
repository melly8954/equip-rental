package com.equip.equiprental.rental.repository.dsl;

import com.equip.equiprental.common.dto.SearchParamDto;
import com.equip.equiprental.equipment.domain.QCategory;
import com.equip.equiprental.equipment.domain.QEquipment;
import com.equip.equiprental.equipment.domain.QEquipmentItem;
import com.equip.equiprental.equipment.domain.QSubCategory;
import com.equip.equiprental.filestorage.domain.QFileMeta;
import com.equip.equiprental.member.domain.QDepartment;
import com.equip.equiprental.member.domain.QMember;
import com.equip.equiprental.rental.domain.*;
import com.equip.equiprental.rental.dto.AdminRentalItemDto;
import com.equip.equiprental.rental.dto.ReturnedRentalItemDto;
import com.equip.equiprental.rental.dto.UserRentalItemDto;
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
public class RentalItemQRepoImpl implements RentalItemQRepo{

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<AdminRentalItemDto> findAdminRentalItems(SearchParamDto paramDto, Pageable pageable) {
        QRentalItem i = QRentalItem.rentalItem;
        QRental r = QRental.rental;
        QEquipmentItem ei = QEquipmentItem.equipmentItem;
        QEquipment e = QEquipment.equipment;
        QMember m = QMember.member;
        QDepartment d = QDepartment.department;
        QCategory c = QCategory.category;
        QSubCategory sc = QSubCategory.subCategory;
        QFileMeta f = QFileMeta.fileMeta;

        BooleanBuilder builder = new BooleanBuilder();

        if (paramDto.getDepartmentId() != null ) {
            builder.and(d.departmentId.eq(paramDto.getDepartmentId()));
        }

        if (paramDto.getMemberName() != null && !paramDto.getMemberName().isEmpty()) {
            builder.and(m.name.containsIgnoreCase(paramDto.getMemberName()));
        }

        if (paramDto.getCategoryId() != null) {
            builder.and(c.categoryId.eq(paramDto.getCategoryId()));
        }

        if (paramDto.getSubCategoryId() != null) {
            builder.and(sc.subCategoryId.eq(paramDto.getSubCategoryId()));
        }

        if (paramDto.getRentalItemStatus() != null) {
            builder.and(i.status.eq(paramDto.getRentalItemStatus()));
        }

        List<AdminRentalItemDto> results = queryFactory
                .select(Projections.constructor(AdminRentalItemDto.class,
                        i.rentalItemId,
                        r.rentalId,
                        f.filePath,
                        c.label,
                        sc.label,
                        e.model,
                        ei.serialNumber,
                        m.name,
                        d.departmentName,
                        i.startDate,
                        i.endDate,
                        i.actualReturnDate,
                        i.status,
                        i.isExtended
                ))
                .from(i)
                .join(i.rental, r)
                .join(i.equipmentItem, ei)
                .join(r.equipment, e)
                .join(r.member, m)
                .leftJoin(m.department, d)
                .leftJoin(e.subCategory, sc)
                .leftJoin(sc.category, c)
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
                .join(i.rental, r)
                .join(i.equipmentItem, ei)
                .join(r.equipment, e)
                .join(r.member, m)
                .leftJoin(m.department, d)
                .leftJoin(e.subCategory, sc)
                .leftJoin(sc.category, c)
                .where(builder)
                .fetchOne();
        total = total == null ? 0 : total;

        return new PageImpl<>(results, pageable, total);
    }

    @Override
    public Page<UserRentalItemDto> findUserRentalItems(Pageable pageable, Long rentalId, Long memberId) {
        QRentalItem i = QRentalItem.rentalItem;
        QEquipment e = QEquipment.equipment;
        QSubCategory sc = QSubCategory.subCategory;
        QCategory c = QCategory.category;
        QFileMeta f = QFileMeta.fileMeta;

        BooleanBuilder builder = new BooleanBuilder();

        builder.and(i.rental.member.memberId.eq(memberId));
        builder.and(i.rental.rentalId.eq(rentalId));
        builder.and(i.rental.status.eq(RentalStatus.APPROVED));

        List<UserRentalItemDto> results = queryFactory
                .select(Projections.constructor(UserRentalItemDto.class,
                        i.rentalItemId,
                        i.rental.rentalId,
                        f.filePath,
                        c.label,
                        sc.label,
                        e.model,
                        i.equipmentItem.serialNumber,
                        i.startDate,
                        i.endDate,
                        i.actualReturnDate,
                        i.status,
                        i.isExtended
                ))
                .from(i)
                .join(i.rental.equipment, e)
                .leftJoin(e.subCategory, sc)
                .leftJoin(sc.category, c)
                .leftJoin(f).on(f.relatedType.eq("equipment").and(f.relatedId.eq(i.rental.equipment.equipmentId)))
                .where(builder)
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .orderBy(i.createdAt.desc(), i.endDate.desc())
                .fetch();

        // 카운트 조회
        Long total = queryFactory
                .select(i.count())
                .from(i)
                .join(i.rental.equipment, e)
                .leftJoin(e.subCategory, sc)
                .leftJoin(sc.category, c)
                .where(builder)
                .fetchOne();
        total = total == null ? 0 : total;

        return new PageImpl<>(results, pageable, total);
    }

    @Override
    public Page<ReturnedRentalItemDto> findReturnRentalItems(Pageable pageable, Long rentalId, Long memberId) {
        QRentalItem i = QRentalItem.rentalItem;
        QEquipment e = QEquipment.equipment;
        QSubCategory sc = QSubCategory.subCategory;
        QCategory c = QCategory.category;
        QFileMeta f = QFileMeta.fileMeta;
        QRentalItemOverdue o = QRentalItemOverdue.rentalItemOverdue;

        BooleanBuilder builder = new BooleanBuilder();
        builder.and(i.rental.rentalId.eq(rentalId));
        builder.and(i.rental.member.memberId.eq(memberId));
        builder.and(i.status.eq(RentalItemStatus.RETURNED));

        List<ReturnedRentalItemDto> results = queryFactory
                .select(Projections.constructor(ReturnedRentalItemDto.class,
                        i.rentalItemId,
                        i.rental.rentalId,
                        f.filePath,
                        c.label,
                        sc.label,
                        e.model,
                        i.equipmentItem.serialNumber,
                        i.startDate,
                        i.endDate,
                        i.actualReturnDate,
                        i.status,
                        i.isExtended,
                        o.overdueDays
                ))
                .from(i)
                .join(i.rental.equipment, e)
                .leftJoin(e.subCategory, sc)
                .leftJoin(sc.category, c)
                .leftJoin(f).on(f.relatedType.eq("equipment").and(f.relatedId.eq(i.rental.equipment.equipmentId)))
                .leftJoin(o).on(o.rentalItem.eq(i))
                .where(builder)
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .orderBy(i.actualReturnDate.desc())
                .fetch();

        Long total = queryFactory
                .select(i.count())
                .from(i)
                .where(builder)
                .fetchOne();

        return new PageImpl<>(results, pageable, total != null ? total : 0);
    }
}
