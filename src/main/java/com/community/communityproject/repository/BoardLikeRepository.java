package com.community.communityproject.repository;

import com.community.communityproject.entity.board.Board;
import com.community.communityproject.entity.board.BoardLike;
import com.community.communityproject.entity.board.LikeStatus;
import com.community.communityproject.entity.users.Users;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface BoardLikeRepository extends JpaRepository<BoardLike, Long> {

    Optional<BoardLike> findByBoardAndUsers(Board board, Users users);

    // 프로필에서 좋아요 누른 게시글 찾는 용도
    @Query("SELECT bl.board FROM BoardLike bl WHERE bl.users = :users AND bl.isLiked = true")
    Page<Board> findBoardLikesByUsers(@Param("users") Users users, Pageable pageable);
}
