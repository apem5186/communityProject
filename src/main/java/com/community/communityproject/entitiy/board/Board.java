package com.community.communityproject.entitiy.board;

import com.community.communityproject.entitiy.users.Users;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PUBLIC)
@Table
public class Board {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String content;

    private Long hits;

    private Long reviewCnt;

    private Long likeCnt;

    @NotNull
    @Enumerated(EnumType.STRING)
    private Category category;

    @OneToMany(mappedBy = "board", cascade = CascadeType.REMOVE)
    private List<BoardImage> boardImages = new ArrayList<>();

    @ManyToOne(targetEntity = Users.class)
    private Users users;

    @Builder
    public Board (String title, String content, Category category, Users users) {
        this.title = title;
        this.content = content;
        this.category = category;
        this.users = users;
    }
}
