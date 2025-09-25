package com.equip.equiprental.board.repository;

import com.equip.equiprental.board.domain.Comment;
import com.equip.equiprental.board.repository.dsl.CommentQRepo;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommentRepository extends JpaRepository<Comment, Long>, CommentQRepo {
}
