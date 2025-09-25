package com.equip.equiprental.board.repository.dsl;

import com.equip.equiprental.board.domain.Comment;
import com.equip.equiprental.board.domain.QComment;
import com.equip.equiprental.board.dto.CommentListResponse;
import com.equip.equiprental.board.dto.QCommentListResponse;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class CommentQRepoImpl implements CommentQRepo {
    private final JPAQueryFactory queryFactory;


    @Override
    public Page<CommentListResponse> findCommentList(Pageable pageable, Long boardId, Long writerId) {
        QComment c = QComment.comment;

        BooleanBuilder builder = new BooleanBuilder();

        if(boardId != null) {
            builder.and(c.board.boardId.eq(boardId));
        }

        builder.and(c.parent.isNull());

        List<CommentListResponse> contents = queryFactory
                .select(new QCommentListResponse(
                        c.commentId,
                        c.writer.memberId,
                        c.writer.name,
                        c.content,
                        c.isOfficial,
                        c.writer.memberId.eq(writerId),
                        c.createdAt,
                        c.updatedAt
                ))
                .from(c)
                .where(builder)
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .orderBy(c.createdAt.desc())
                .fetch();

        // 페이징 계산용 total count
        Long total = queryFactory
                .select(c.count())
                .from(c)
                .where(builder)
                .fetchOne();
        total = total == null ? 0 : total;

        // 부모 댓글 ID 리스트 추출
        List<Long> parentIds = contents.stream()
                .map(CommentListResponse::getCommentId)
                .toList();

        if (!parentIds.isEmpty()) {
            // 대댓글 조회
            List<Comment> childComments = queryFactory
                    .selectFrom(c)
                    .where(c.parent.commentId.in(parentIds))
                    .orderBy(c.createdAt.asc()) // 오래된 순
                    .fetch();

            // 부모 댓글 DTO에 대댓글 매핑
            contents.forEach(parent -> {
                parent.getChildren().addAll(fetchChildren(parent.getCommentId(), writerId));
            });
        }

        // 최종 Page 반환
        return new PageImpl<>(contents, pageable, total);
    }

    // 재귀적으로 children 채우기
    private List<CommentListResponse> fetchChildren(Long parentId, Long writerId) {
        QComment c = QComment.comment;

        List<Comment> childComments = queryFactory
                .selectFrom(c)
                .where(c.parent.commentId.eq(parentId))
                .orderBy(c.createdAt.asc())
                .fetch();

        return childComments.stream()
                .map(child -> new CommentListResponse(
                        child.getCommentId(),
                        child.getWriter().getMemberId(),
                        child.getWriter().getName(),
                        child.getContent(),
                        child.getIsOfficial(),
                        child.getWriter().getMemberId().equals(writerId),
                        child.getCreatedAt(),
                        child.getUpdatedAt(),
                        fetchChildren(child.getCommentId(), writerId) // 재귀
                ))
                .toList();
    }
}
