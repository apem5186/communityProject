package com.community.communityproject.repository;

import com.community.communityproject.entitiy.board.Board;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BoardRepository extends JpaRepository<Board, Long> {
}
