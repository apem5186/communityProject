package com.community.communityproject.repository;

import com.community.communityproject.entitiy.board.BoardImage;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BoardImageRepository extends JpaRepository<BoardImage, Long> {
}
