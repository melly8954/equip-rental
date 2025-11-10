package com.equip.equiprental.board.repository;

import com.equip.equiprental.board.domain.Board;
import com.equip.equiprental.board.repository.dsl.BoardQRepo;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BoardRepository extends JpaRepository<Board, Long>, BoardQRepo {
}
