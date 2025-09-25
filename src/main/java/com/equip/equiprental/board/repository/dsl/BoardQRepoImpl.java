package com.equip.equiprental.board.repository.dsl;

import com.equip.equiprental.board.domain.BoardType;
import com.equip.equiprental.board.domain.QBoard;
import com.equip.equiprental.board.dto.BoardListResponse;
import com.equip.equiprental.common.dto.SearchParamDto;
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
public class BoardQRepoImpl implements BoardQRepo{
    private final JPAQueryFactory queryFactory;

    @Override
    public Page<BoardListResponse> findBoardList(Pageable pageable, SearchParamDto paramDto) {
        QBoard b = QBoard.board;

        BooleanBuilder builder = new BooleanBuilder();

        if (paramDto.getBoardType() != null) {
            builder.and(b.boardType.eq(paramDto.getBoardType()));
        }

        if (paramDto.getKeyword() != null && !paramDto.getKeyword().isBlank()) {
            if ("title".equalsIgnoreCase(paramDto.getSearchType())) {
                builder.and(b.title.containsIgnoreCase(paramDto.getKeyword()));
            } else if ("writer".equalsIgnoreCase(paramDto.getSearchType())) {
                builder.and(b.writer.name.containsIgnoreCase(paramDto.getKeyword()));
            }
        }

        builder.and(b.isDeleted.eq(false));

        List<BoardListResponse> contents = queryFactory
                .select(Projections.constructor(BoardListResponse.class,
                        b.boardId,
                        b.boardType,
                        b.writer.name,
                        b.title,
                        b.createdAt))
                .from(b)
                .where(builder)
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .orderBy(b.createdAt.desc())
                .fetch();

        Long total = queryFactory
                .select(b.count())
                .from(b)
                .where(builder)
                .fetchOne();
        total = total == null ? 0 : total;

        return new PageImpl<>(contents, pageable, total);
    }

    @Override
    public List<BoardListResponse> findLatestNotices(int limit) {
        QBoard b = QBoard.board;

        return queryFactory
                .select(Projections.constructor(BoardListResponse.class,
                        b.boardId,
                        b.boardType,
                        b.writer.name,
                        b.title,
                        b.createdAt))
                .from(b)
                .where(b.boardType.eq(BoardType.NOTICE)
                        .and(b.isDeleted.eq(false)))
                .orderBy(b.createdAt.desc())
                .limit(limit)
                .fetch();
    }
}
