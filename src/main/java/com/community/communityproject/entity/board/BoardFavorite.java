package com.community.communityproject.entity.board;

import com.community.communityproject.entity.users.Users;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.springframework.data.annotation.CreatedDate;

import java.time.LocalDateTime;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class BoardFavorite {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "board_id")
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Board board;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "users_id")
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Users users;

    @Column(nullable = false)
    private boolean status;

    @CreatedDate
    @Column(name = "regDate", updatable = false)
    private LocalDateTime regDate;

    @PrePersist
    public void onPrePersist() {
        this.regDate = LocalDateTime.now();
    }

    public BoardFavorite(Board board, Users users) {
        this.board = board;
        this.users = users;
        this.status = true;
    }
}
