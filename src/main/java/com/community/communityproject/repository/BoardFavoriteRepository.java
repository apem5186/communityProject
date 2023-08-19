package com.community.communityproject.repository;

import com.community.communityproject.entitiy.board.Board;
import com.community.communityproject.entitiy.board.BoardFavorite;
import com.community.communityproject.entitiy.users.Users;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface BoardFavoriteRepository extends JpaRepository<BoardFavorite, Long> {

    Optional<BoardFavorite> findByBoardAndUsers(Board board, Users users);
    Optional<BoardFavorite> findByBoard_idAndUsers_id(Long board_id, Long users_id);

    // 프로필에서 즐겨찾기한 게시글 찾는 용도
    @Query("SELECT fb.board FROM BoardFavorite fb WHERE fb.users.id = :usersId AND fb.status = true")
    List<Board> findBoardFavoritesByByUsersId(@Param("usersId") Long userId);
}
