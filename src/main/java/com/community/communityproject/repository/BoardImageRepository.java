package com.community.communityproject.repository;

import com.community.communityproject.entitiy.board.Board;
import com.community.communityproject.entitiy.board.BoardImage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.ArrayList;

public interface BoardImageRepository extends JpaRepository<BoardImage, Long> {
    ArrayList<BoardImage> findBoardImageByBoardId(Long bid);
}
