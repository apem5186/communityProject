package com.community.communityproject.repository;

import com.community.communityproject.entitiy.board.Board;
import com.community.communityproject.entitiy.board.BoardLike;
import com.community.communityproject.entitiy.users.Users;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BoardLikeRepository extends JpaRepository<BoardLike, Long> {

    Optional<BoardLike> findByBoardAndUsers(Board board, Users users);
}
