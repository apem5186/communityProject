package com.community.communityproject.entity.comment;

import com.community.communityproject.entity.board.LikeStatus;
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
public class CommentLike {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "comment_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Comment comment;

    @ManyToOne
    @JoinColumn(name = "users_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Users users;

    @Column(nullable = false)
    private boolean isLiked;

    @Column
    @Enumerated(EnumType.STRING)
    private LikeStatus likeStatus;

    @CreatedDate
    @Column(name = "regDate", updatable = false)
    private LocalDateTime regDate;

    @PrePersist
    public void onPrePersist() {
        this.regDate = LocalDateTime.now();
    }

    public CommentLike(Comment comment,  Users users, LikeStatus likeStatus) {
        this.comment = comment;
        this.users = users;
        this.isLiked = true;
        this.likeStatus = likeStatus;
    }


}
