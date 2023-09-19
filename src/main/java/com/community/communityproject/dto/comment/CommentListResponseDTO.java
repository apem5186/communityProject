package com.community.communityproject.dto.comment;

import com.community.communityproject.entity.board.Board;
import com.community.communityproject.entity.comment.Comment;
import com.community.communityproject.entity.users.ProfileImage;
import com.community.communityproject.entity.users.Users;
import lombok.*;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

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
        private String likeStatus;
        private boolean isDeleted;
        private String childLikeStatus;
        private List<CommentDTO> children;
        private Long parent;
        private LocalDateTime regDate;
        private LocalDateTime modDate;

        public CommentDTO(Comment comment) {
            this.cid = comment.getId();
            this.content = comment.getContent();
            this.likeCnt = comment.getLikeCnt();
            this.usersDTOInComment = new UsersDTOInComment(comment.getUsers());
            this.profileImgDTOInComment = new ProfileImgDTOInComment(comment.getUsers().getProfileImage());
            this.boardDTOInComment = new BoardDTOInComment(comment.getBoard());
            this.isDeleted = comment.isDeleted();
            setChildrenFromEntities(comment.getChildren());
            if (comment.getParent() == null) {
                this.parent = null;
            } else {
                this.parent = comment.getParent().getId();
            }
            this.regDate = comment.getRegDate();
            this.modDate = comment.getModDate();
        }

        public CommentDTO(Comment comment, Function<Long, String> likeStatusFetcher) {
            this.cid = comment.getId();
            this.content = comment.getContent();
            this.likeCnt = comment.getLikeCnt();
            this.usersDTOInComment = new UsersDTOInComment(comment.getUsers());
            this.profileImgDTOInComment = new ProfileImgDTOInComment(comment.getUsers().getProfileImage());
            this.boardDTOInComment = new BoardDTOInComment(comment.getBoard());
            setChildrenFromEntities(comment.getChildren(), likeStatusFetcher);
            this.likeStatus = likeStatusFetcher.apply(comment.getId());
            this.isDeleted = comment.isDeleted();
            if (comment.getParent() == null) {
                this.parent = null;
            } else {
                this.parent = comment.getParent().getId();
            }
            this.regDate = comment.getRegDate();
            this.modDate = comment.getModDate();
        }

        public void setChildrenFromEntities(Set<Comment> children) {
            this.children = children.stream()
                    .sorted(Comparator.comparing(Comment::getRegDate))
                    .map(CommentDTO::new)
                    .collect(Collectors.toList());
        }

        public void setChildrenFromEntities(Set<Comment> children, Function<Long, String> likeStatusFetcher) {
            this.children = children.stream()
                    .sorted(Comparator.comparing(Comment::getRegDate))
                    .map(child -> new CommentDTO(child, likeStatusFetcher))
                    .collect(Collectors.toList());
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
        private String category;

        public BoardDTOInComment(Board board) {
            this.bid = board.getId();
            this.title = board.getTitle();
            this.usernameHasBoard = board.getUsers().getUsername();
            this.emailHasBoard = board.getUsers().getEmail();
            this.category = board.getCategory().toString();
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

    // 나중에 다시 보면 Function<T, R> 에 대해 다시 숙지하기
    public CommentDTO getCommentDTOWithStatus(Comment comment, Function<Long, String> likeStatusFetcher) {
        return new CommentDTO(comment, likeStatusFetcher);
    }

}
