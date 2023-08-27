package com.community.communityproject.entity.comment;

import com.community.communityproject.entity.BaseEntity;
import com.community.communityproject.entity.board.Board;
import com.community.communityproject.entity.users.Users;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.util.ArrayList;
import java.util.List;

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

    @Builder
    public Comment(String content, Board board, Users users) {
        this.content = content;
        this.board = board;
        this.users = users;
    }

    public void increaseLikeCnt() { this.likeCnt += 1;}
    public void decreaseLikeCnt() { this.likeCnt -= 1;}
}
