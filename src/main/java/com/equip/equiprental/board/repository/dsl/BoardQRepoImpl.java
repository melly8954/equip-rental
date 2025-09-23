package com.equip.equiprental.board.repository.dsl;

import com.equip.equiprental.board.domain.Board;
import com.equip.equiprental.board.domain.QBoard;
import com.equip.equiprental.board.dto.BoardListResponse;
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
    public Page<BoardListResponse> findBoardList(Pageable pageable) {
        QBoard b = QBoard.board;

        List<BoardListResponse> contents = queryFactory
                .select(Projections.constructor(BoardListResponse.class,
                        b.boardId,
                        b.boardType,
                        b.writer.name,
                        b.title,
                        b.createdAt))
                .from(b)
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .orderBy(b.createdAt.desc())
                .fetch();

        Long total = queryFactory
                .select(b.count())
                .from(b)
                .fetchOne();
        total = total == null ? 0 : total;

        return new PageImpl<>(contents, pageable, total);
    }
}
