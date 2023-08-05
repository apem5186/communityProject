package com.community.communityproject.dto.board;

import com.community.communityproject.entitiy.board.Board;
import com.community.communityproject.entitiy.board.BoardImage;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.stream.Collectors;

@Getter
@NoArgsConstructor(access = AccessLevel.PUBLIC)
public class BoardListResponseDTO {

    private String title;

    private String content;

    private Long hits;

    private Long reviewCnt;

    private Long likeCnt;

    private String category;

    private List<String> imgPathList;

    private String username;

    private String profileImgPath;

    @Builder
    public BoardListResponseDTO(Board board) {
        List<BoardImage> boardImages = board.getBoardImages();
//        boardImages.forEach(boardImage -> {
//            imgPathList.add(boardImage.getFilePath());
//        });
        this.title = board.getTitle();
        this.content = board.getContent();
        this.hits = board.getHits();
        this.reviewCnt = board.getReviewCnt();
        this.likeCnt = board.getLikeCnt();
        this.category = board.getCategory().toString();
        this.imgPathList = boardImages.stream().map(BoardImage::getFilePath).collect(Collectors.toList());
        this.username = board.getUsers().getUsername();
        this.profileImgPath = board.getUsers().getProfileImage().getFilePath();
    }
}
