package com.community.communityproject.entity.board;

import com.community.communityproject.entity.BaseEntity;
import com.community.communityproject.entity.comment.Comment;
import com.community.communityproject.entity.users.Users;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table
public class Board extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    // columnDefinition = "TEXT"는 글자수를 제한 할 수 없을 때 사용
    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    @Column(columnDefinition = "integer default 0")
    private int hits;

    @Column(columnDefinition = "integer default 0")
    private int likeCnt;

    @Column(columnDefinition = "integer default 0")
    private int reviewCnt;

    @Column(columnDefinition = "integer default 0")
    private int favoriteCnt;

    @ElementCollection(targetClass = Category.class, fetch = FetchType.EAGER)
    @CollectionTable(name = "board_notice_mapping", joinColumns = @JoinColumn(name = "board_id"))
    @Column(name = "notices")
    @Enumerated(EnumType.STRING)
    private Set<Category> notices = new HashSet<>();

    @NotNull
    @Enumerated(EnumType.STRING)
    private Category category;

    @OneToMany(mappedBy = "board", cascade = CascadeType.REMOVE)
    private final List<BoardImage> boardImages = new ArrayList<>();

    @ManyToOne(targetEntity = Users.class)
    private Users users;

    @OneToMany(mappedBy = "board")
    private final List<Comment> comments = new ArrayList<>();

    @OneToMany(mappedBy = "board", cascade = CascadeType.REMOVE)
    private final List<BoardFavorite> boardFavorites = new ArrayList<>();

    @OneToMany(mappedBy = "board", cascade = CascadeType.REMOVE)
    private final List<BoardLike> boardLikes = new ArrayList<>();

    public void edit(String title, String content, String category) {
        this.title = title;
        this.content = content;
        this.category = Category.valueOf(category);
    }

    @Builder
    public Board (String title, String content, Category category, Users users) {
        this.title = title;
        this.content = content;
        this.category = category;
        this.users = users;
    }

    @Builder(builderMethodName = "NoticeBuilder")
    public Board (String title, String content, Category category, Users users, Set<Category> notices) {
        this.title = title;
        this.content = content;
        this.category = category;
        this.users = users;
        this.notices = notices;
    }
    
    // 테스트 게시글 생성용
    @Builder(builderMethodName = "TestBuilder")
    public Board (String title, String content, Category category, Users users, int hits,
                  int likeCnt, int reviewCnt, int favoriteCnt) {
        this.title = title;
        this.content = content;
        this.category = category;
        this.users = users;
        this.hits = hits;
        this.likeCnt = likeCnt;
        this.reviewCnt = reviewCnt;
        this.favoriteCnt = favoriteCnt;
    }

    public void testBoardEdit (int hits, int likeCnt, int reviewCnt, int favoriteCnt) {
        this.hits = hits;
        this.likeCnt = likeCnt;
        this.reviewCnt = reviewCnt;
        this.favoriteCnt = favoriteCnt;
    }

    public void increaseLikeCnt() {
        this.likeCnt += 1;
    }

    public void decreaseLikeCnt() {
        this.likeCnt -= 1;
    }

    public void increaseFavoriteCnt() {this.favoriteCnt += 1;}

    public void decreaseFavoriteCnt() {this.favoriteCnt -= 1;}

    public void increaseReviewCnt() { this.reviewCnt += 1; }

    public void decreaseReviewCnt() { this.reviewCnt -= 1; }
}
