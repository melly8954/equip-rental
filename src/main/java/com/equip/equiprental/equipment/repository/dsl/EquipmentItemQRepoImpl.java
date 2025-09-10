package com.equip.equiprental.equipment.repository.dsl;

import com.equip.equiprental.common.dto.PageResponseDto;
import com.equip.equiprental.equipment.domain.*;
import com.equip.equiprental.equipment.dto.EquipmentDto;
import com.equip.equiprental.equipment.dto.EquipmentItemDto;
import com.equip.equiprental.equipment.dto.EquipmentItemListDto;
import com.equip.equiprental.equipment.repository.EquipmentRepository;
import com.equip.equiprental.filestorage.domain.QFileMeta;
import com.equip.equiprental.filestorage.repository.FileRepository;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.JPQLQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class EquipmentItemQRepoImpl implements EquipmentItemQRepo {

    private final JPAQueryFactory queryFactory;
    private final EquipmentRepository equipmentRepository;
    private final FileRepository fileRepository;

    @Override
    public EquipmentItemListDto findEquipmentItemByFilter(Long equipmentId, EquipmentStatus status, Pageable pageable) {
        QEquipmentItem item = QEquipmentItem.equipmentItem;
        QEquipment equipment = QEquipment.equipment;

        BooleanBuilder builder = new BooleanBuilder();
        builder.and(item.equipment.equipmentId.eq(equipmentId));
        if (status != null) {
            builder.and(item.status.eq(status));
        }

        // fetch join + 페이징
        JPQLQuery<EquipmentItem> query = queryFactory
                .selectFrom(item)
                .leftJoin(item.equipment, equipment).fetchJoin()
                .where(builder)
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize());

        List<EquipmentItem> itemList = query.fetch();

        Long total = queryFactory
                .select(item.count())
                .from(item)
                .where(builder)
                .fetchOne();
        total = (total != null) ? total : 0L;

        List<EquipmentItemDto> content = itemList.stream()
                .map(i -> EquipmentItemDto.builder()
                        .equipmentItemId(i.getEquipmentItemId())
                        .serialNumber(i.getSerialNumber())
                        .status(i.getStatus())
                        .build())
                .toList();

        Equipment e = itemList.isEmpty() ?
                equipmentRepository.findById(equipmentId)
                        .orElseThrow(() -> new RuntimeException("장비가 존재하지 않습니다."))
                : itemList.get(0).getEquipment();

        EquipmentDto equipmentSummary = EquipmentDto.builder()
                .equipmentId(e.getEquipmentId())
                .category(e.getCategory().name())
                .subCategory(e.getSubCategory())
                .model(e.getModel())
                .stock(e.getStock())
                .imageUrl(fileRepository.findUrlsByEquipmentId(equipmentId)
                        .stream().findFirst().orElse(null))
                .build();

        return EquipmentItemListDto.builder()
                .equipmentSummary(equipmentSummary)
                .equipmentItems(
                        PageResponseDto.<EquipmentItemDto>builder()
                                .content(content)
                                .page(pageable.getPageNumber() + 1)
                                .size(pageable.getPageSize())
                                .totalElements(total)
                                .totalPages((int) Math.ceil((double) total / pageable.getPageSize()))
                                .numberOfElements(content.size())
                                .first(pageable.getPageNumber() == 0)
                                .last((pageable.getOffset() + content.size()) >= total)
                                .empty(content.isEmpty())
                                .build()
                )
                .build();
    }
}
