package com.community.communityproject.entitiy.board;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PUBLIC)
@Entity
@Table
public class BoardImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String originFilename;

    private String filePath;

    private Long fileSize;

    @ManyToOne
    @JoinColumn(name = "board_id")
    private Board board;
}
