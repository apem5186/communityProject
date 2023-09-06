package com.community.communityproject.repository;

import com.community.communityproject.entity.board.Board;
import com.community.communityproject.entity.users.Users;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface BoardRepository extends JpaRepository<Board, Long> {

    Page<Board> findAll(Specification<Board> spec, Pageable pageable);

    Page<Board> findAllByUsersEmail(String email, Pageable pageable);

    List<Board> findAllByUsers(Users users);

    @Modifying
    @Query("update Board b set b.hits = b.hits + 1 where b.id =:id")
    int updateHits(@Param("id") Integer id);
}
