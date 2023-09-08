package com.community.communityproject.repository;

import com.community.communityproject.entity.board.Board;
import com.community.communityproject.entity.comment.Comment;
import com.community.communityproject.entity.users.Users;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    Page<Comment> findAllByBoard(Board board, Pageable pageable);
    List<Comment> findAllByParent(Comment parent);
    Page<Comment> findAllByBoardAndParentIsNull(Board board, Pageable pageable);
    Page<Comment> findAllByUsersEmail(String email, Pageable pageable);
    List<Comment> findAllByUsers(Users users);
    Page<Comment> findAllByUsersId(Long uid, Pageable pageable);
}
