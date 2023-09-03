package com.community.communityproject.entity.comment;

import com.community.communityproject.entity.BaseEntity;
import com.community.communityproject.entity.board.Board;
import com.community.communityproject.entity.users.Users;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Table
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Comment extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String content;

    @Column(columnDefinition = "integer default 0")
    private int likeCnt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "board_id")
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Board board;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "users_id")
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Users users;

    @OneToMany(mappedBy = "comment")
    private final List<CommentLike> commentLikes = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Comment parent;

    @OneToMany(mappedBy = "parent")
    @ToString.Exclude
    private Set<Comment> children = new LinkedHashSet<>();

    @Builder
    public Comment(String content, Board board, Users users, Comment parent) {
        this.content = content;
        this.board = board;
        this.users = users;
        this.parent = parent;
    }

    // 댓글 수정
    public void edit(String content) {
        this.content = content;
    }
    public void increaseLikeCnt() { this.likeCnt += 1;}
    public void decreaseLikeCnt() { this.likeCnt -= 1;}
}
