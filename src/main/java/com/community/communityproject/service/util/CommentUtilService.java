package com.community.communityproject.service.util;

import com.community.communityproject.entity.board.Board;
import com.community.communityproject.entity.board.Category;
import com.community.communityproject.entity.comment.Comment;
import com.community.communityproject.entity.users.UserRole;
import com.community.communityproject.entity.users.Users;
import jakarta.persistence.criteria.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.io.Serial;

@Slf4j
@Service
@RequiredArgsConstructor
public class CommentUtilService {

    public String sort(String sort) {
        return switch (sort) {
            case "VOTE_COUNT" -> "likeCnt";
            case "REPLY_COUNT" -> "childrenCnt";
            default -> "regDate";
        };
    }

    public Specification<Comment> search(String kw, String searchField) {
        return new Specification<Comment>() {
            @Serial
            private static final long serialVersionUID = 1L;
            @Override
            public Predicate toPredicate(Root<Comment> q, CriteriaQuery<?> query, CriteriaBuilder cb) {
                query.distinct(true);   // 중복제거
                Join<Comment, Users> u1 = q.join("users", JoinType.LEFT);
                // 추가 예정
                //Join<Board, Comment> c = q.join("commentList", JoinType.LEFT);

                if (kw == null || kw.isEmpty()) {
                    return null; // 모든 결과를 반환
                }

                Predicate predicate = null;
                switch (searchField) {

                    case "id" -> predicate = cb.like(cb.toString(q.get("id")), "%" + kw + "%");
                    case "boardId" -> predicate = cb.like(cb.toString(q.get("board")), "%" + kw + "%");
                    case "username" -> predicate = cb.like(u1.get("username"), "%" + kw + "%");
                    case "ALL" -> {
                        return null;
                    }
                    default -> throw new IllegalArgumentException("Unknown search field");
                }
                return predicate;
                // 추가 예정
                //cb.like(c.get("content"), "%" + kw + "%"),   // 댓글 내용
                // 작성자는 필요하면 추가
                // cb.like(u1.get("username"), "%" + kw + "%")); // 게시글 작성자
            }
        };
    }

    public Specification<Comment> category(String category) {
        return new Specification<Comment>() {
            @Override
            public Predicate toPredicate(Root<Comment> q, CriteriaQuery<?> query, CriteriaBuilder cb) {
                Join<Comment, Board> boardJoin = q.join("board", JoinType.INNER);
                return cb.equal(boardJoin.get("category"), Category.valueOf(category.toUpperCase()));
            }
        };
    }

    public Specification<Comment> filterByUserRole(String userRole) {
        return (root, query, cb) -> {
            Join<Comment, Users> userJoin = root.join("users");
            return cb.equal(userJoin.get("userRole"), UserRole.valueOf(userRole));
        };
    }

    public Specification<Comment> filterByOption2(String option2) {
        return (root, query, cb) -> {
            Predicate predicate = null;
            switch (option2) {
                case "PARENT" -> predicate = cb.isNull(root.get("parent"));
                case "CHILDREN" -> predicate = cb.isNotNull(root.get("parent"));
                case "DELETED" -> predicate = cb.isTrue(root.get("isDeleted"));
                case "EXISTED" -> predicate = cb.isFalse(root.get("isDeleted"));
                default -> {
                    return null;
                }
            }
            return predicate;
        };
    }
}
