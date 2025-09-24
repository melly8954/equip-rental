package com.equip.equiprental.board.domain;

import com.equip.equiprental.common.domain.BaseEntity;
import com.equip.equiprental.member.domain.Member;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name="board_tbl")
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Board extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="board_id")
    private Long boardId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="writer_id")
    private Member writer;

    @Enumerated(EnumType.STRING)
    @Column(name="board_type")
    private BoardType boardType;

    private String title;
    private String content;

    @Enumerated(EnumType.STRING)
    private BoardStatus status;

    @Column(name="is_deleted")
    private Boolean isDeleted;

    @Column(name="deleted_at")
    private LocalDateTime deletedAt;

    public void softDelete() {
        this.isDeleted = true;
        this.deletedAt = LocalDateTime.now();
    }
}
