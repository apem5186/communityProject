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
import org.springframework.security.core.parameters.P;

import java.util.List;
import java.util.Optional;

public interface BoardLikeRepository extends JpaRepository<BoardLike, Long> {

    Optional<BoardLike> findByBoardAndUsers(Board board, Users users);

    @Query("SELECT bl.board FROM BoardLike bl WHERE bl.users.id = :uid")
    List<Board> findBoardLikesByUsersId(@Param("usersId") Long uid);

    List<BoardLike> findAllByUsers(Users users);

    // 프로필에서 좋아요 누른 게시글 찾는 용도
    @Query("SELECT bl.board FROM BoardLike bl WHERE bl.users = :users AND bl.isLiked = true")
    Page<Board> findBoardLikesByUsers(@Param("users") Users users, Pageable pageable);

    // 남의 프로필에서 좋아요 누른 게시글 찾는 용도
    @Query("SELECT bl.board FROM BoardLike bl WHERE bl.users.id = :uid AND bl.isLiked = true")
    Page<Board> findBoardLikesByUsersId(@Param("usersId") Long uid, Pageable pageable);
}
