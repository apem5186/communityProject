package com.community.communityproject.dto.comment;

import com.community.communityproject.entity.board.Board;
import com.community.communityproject.entity.comment.Comment;
import com.community.communityproject.entity.users.ProfileImage;
import com.community.communityproject.entity.users.Users;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PUBLIC)
public class CommentListResponseDTO {

    @Getter
    @Setter
    public class CommentDTO {
        private Long cid;
        private String content;
        private int likeCnt;
        private UsersDTOInComment usersDTOInComment;
        private BoardDTOInComment boardDTOInComment;
        private ProfileImgDTOInComment profileImgDTOInComment;

        public CommentDTO(Comment comment) {
            this.cid = comment.getId();
            this.content = comment.getContent();
            this.likeCnt = comment.getLikeCnt();
            this.usersDTOInComment = new UsersDTOInComment(comment.getUsers());
            this.profileImgDTOInComment = new ProfileImgDTOInComment(comment.getUsers().getProfileImage());
            this.boardDTOInComment = new BoardDTOInComment(comment.getBoard());
        }

    }

    @Getter
    @Setter
    public class UsersDTOInComment{
        private String username;
        private String email;

        public UsersDTOInComment(Users users) {
            this.username = users.getUsername();
            this.email = users.getEmail();
        }
    }

    @Getter
    @Setter
    public class BoardDTOInComment{
        private Long bid;
        private String title;
        private String usernameHasBoard;
        private String emailHasBoard;

        public BoardDTOInComment(Board board) {
            this.bid = board.getId();
            this.title = board.getTitle();
            this.usernameHasBoard = board.getUsers().getUsername();
            this.emailHasBoard = board.getUsers().getEmail();
        }
    }

    @Getter
    @Setter
    public class ProfileImgDTOInComment{
        private String path;

        public ProfileImgDTOInComment(ProfileImage profileImage) {
            this.path = profileImage.getFilePath();
        }
    }

    public CommentDTO getCommentDTO(Comment comment) {
        return new CommentDTO(comment);
    }

}
