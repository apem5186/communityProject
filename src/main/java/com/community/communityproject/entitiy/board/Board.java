package com.community.communityproject.entitiy.board;

import com.community.communityproject.entitiy.BaseEntity;
import com.community.communityproject.entitiy.users.Users;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

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

    @NotNull
    @Enumerated(EnumType.STRING)
    private Category category;

    @OneToMany(mappedBy = "board", cascade = CascadeType.REMOVE)
    private List<BoardImage> boardImages = new ArrayList<>();

    @ManyToOne(targetEntity = Users.class)
    private Users users;

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

    public void increaseLikeCnt() {
        this.likeCnt += 1;
    }

    public void decreaseLikeCnt() {
        this.likeCnt -= 1;
    }
}
