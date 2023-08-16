package com.community.communityproject.repository;

import com.community.communityproject.entitiy.board.Board;
import com.community.communityproject.entitiy.board.BoardFavorite;
import com.community.communityproject.entitiy.users.Users;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BoardFavoriteRepository extends JpaRepository<BoardFavorite, Long> {

    Optional<BoardFavorite> findByBoardAndUsers(Board board, Users users);
    Optional<BoardFavorite> findByBoard_idAndUsers_id(Long board_id, Long users_id);
}
