package com.community.communityproject.entitiy.board;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
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

    @Builder
    public BoardImage(String originFilename, String filePath, Long fileSize,
                      Board board) {
        this.originFilename = originFilename;
        this.filePath = filePath;
        this.fileSize = fileSize;
        this.board = board;
    }
}
