package com.community.communityproject.dto.board;

import com.community.communityproject.entitiy.board.Board;
import com.community.communityproject.entitiy.board.BoardImage;
import com.community.communityproject.entitiy.users.ProfileImage;
import com.community.communityproject.entitiy.users.Users;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PUBLIC)
public class BoardListResponseDTO {

    @Getter
    @Setter
    public class BoardDTO {
        private Long bid;
        private String title;
        private String content;
        private int hits;
        private int reviewCnt;
        private int likeCnt;
        private String category;
        private List<String> imgPathList;
        private UsersDTO users;
        private ProfileImgDTO profilePath;

        public BoardDTO(Board board) {
            this.bid = board.getId();
            this.title = board.getTitle();
            this.content = board.getContent();
            this.hits = board.getHits();
            this.reviewCnt = board.getReviewCnt();
            this.likeCnt = board.getLikeCnt();
            this.category = board.getCategory().toString();
            this.imgPathList = new ArrayList<>();
            for(BoardImage image : board.getBoardImages()) {
                this.imgPathList.add(image.getFilePath());
            }
            this.users = new UsersDTO(board.getUsers());
            this.profilePath = new ProfileImgDTO(board.getUsers().getProfileImage());
        }
    }

    @Getter
    @Setter
    public class UsersDTO{
        private String username;
        private String email;

        public UsersDTO(Users users) {
            this.username = users.getUsername();
            this.email = users.getEmail();
        }
    }

    @Getter
    @Setter
    public class ProfileImgDTO{
        private String path;

        public ProfileImgDTO(ProfileImage profileImage) {
            this.path = profileImage.getFilePath();
        }
    }

    public BoardDTO getBoardDTO(Board board) {
        return new BoardDTO(board);
    }

}
