package com.community.communityproject.repository;

import com.community.communityproject.entity.board.Board;
import com.community.communityproject.entity.comment.Comment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    Page<Comment> findAllByBoard(Board board, Pageable pageable);

    Page<Comment> findAllByUsersEmail(String email, Pageable pageable);
}
