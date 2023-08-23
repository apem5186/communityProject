package com.community.communityproject.repository;

import com.community.communityproject.entity.board.Board;
import com.community.communityproject.entity.board.BoardLike;
import com.community.communityproject.entity.comment.Comment;
import com.community.communityproject.entity.comment.CommentLike;
import com.community.communityproject.entity.users.Users;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CommentLikeRepository extends JpaRepository<CommentLike, Long> {

    Optional<CommentLike> findByCommentAndUsers(Comment comment, Users users);

}
