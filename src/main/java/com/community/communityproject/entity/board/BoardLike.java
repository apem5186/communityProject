package com.community.communityproject.entity.board;

import com.community.communityproject.entity.users.Users;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.springframework.data.annotation.CreatedDate;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class BoardLike {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "board_id", nullable = false)
    private Board board;

    @ManyToOne(fetch = FetchType.LAZY)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "users_id", nullable = false)
    private Users users;

    @Column(nullable = false)
    private boolean isLiked;

    @Column
    private String isRecommend;

    @CreatedDate
    @Column(name = "regDate", updatable = false)
    private LocalDateTime regDate;

    @PrePersist
    public void onPrePersist() {
        this.regDate = LocalDateTime.now();
    }

    public BoardLike(Board board, Users users, String isRecommend) {
        this.board = board;
        this.users = users;
        this.isLiked = true;
        this.isRecommend = isRecommend;
    }

    public void likeBoard() {
        this.isLiked = true;
    }

    public void dislikeBoard() {
        this.isLiked = false;
    }

}
