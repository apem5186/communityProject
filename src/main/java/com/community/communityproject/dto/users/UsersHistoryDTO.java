package com.community.communityproject.dto.users;

import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UsersHistoryDTO {

    @Getter
    @Setter
    @Builder
    public static class BoardHistoryDTO {
        private Long bId;
        private String boardTitle;
        private String boardCategory;
        private LocalDateTime regDate;
        private LocalDateTime modDate;
    }

    @Getter
    @Setter
    @Builder
    public static class CommentHistoryDTO {
        private Long cId;
        private Long bId;
        private String boardOwnerUsername;
        private String boardTitle;
        private String boardCategory;
        private Long commentParentId;  // This would be null for a comment, and have a value for a reply
        private String commentOwnerUsername;
        private LocalDateTime regDate;
        private LocalDateTime modDate;

    }

    @Getter
    @Setter
    @Builder
    public static class BoardLikeHistoryDTO {
        private Long bId;
        private Long blId;
        private String boardTitle;
        private String boardOwnerUsername;
        private String boardCategory;
        private String likeStatus;
        private LocalDateTime regDate;

    }

    @Getter
    @Setter
    @Builder
    public static class BoardFavoriteHistoryDTO {
        private Long bId;
        private Long bfId;
        private String boardTitle;
        private String boardOwnerUsername;
        private String boardCategory;
        private LocalDateTime regDate;

    }

    @Getter
    @Setter
    @Builder
    public static class CommentLikeHistoryDTO{
        private Long cId;
        private Long clId;
        private Long bId;
        private Long commentParentId;
        private String boardTitle;
        private String boardOwnerUsername;
        private String boardCategory;
        private String commentOwnerUsername;
        private String likeStatus;
        private LocalDateTime regDate;

    }

    @Getter
    @Setter
    // Unified DTO to encompass all types of user activities
    public static class UserActivityHistoryDTO {
        private List<BoardHistoryDTO> boardHistories;
        private List<CommentHistoryDTO> commentHistories;  // This list will now include both comments and replies
        private List<BoardLikeHistoryDTO> boardLikeHistories;
        private List<BoardFavoriteHistoryDTO> boardFavoriteHistories;
        private List<CommentLikeHistoryDTO> commentLikeHistories;

        public List<UserActivity> getAllActivities() {
            List<UserActivity> allActivities = new ArrayList<>();

            for (BoardHistoryDTO dto : boardHistories) {
                UserActivity activity = new UserActivity();
                activity.setActivityType("Board");
                activity.setRegDate(dto.getRegDate());
                activity.setCategory(dto.boardCategory.toLowerCase());
                activity.setBoardHistoryDTO(dto);
                allActivities.add(activity);
            }

            for (CommentHistoryDTO dto : commentHistories) {
                UserActivity activity = new UserActivity();
                activity.setActivityType("Comment");
                activity.setRegDate(dto.getRegDate());
                activity.setCategory(dto.boardCategory.toLowerCase());
                activity.setCommentHistoryDTO(dto);
                allActivities.add(activity);
            }

            for (BoardLikeHistoryDTO dto : boardLikeHistories) {
                UserActivity activity = new UserActivity();
                activity.setActivityType("BoardLike");
                activity.setRegDate(dto.getRegDate());
                activity.setCategory(dto.boardCategory.toLowerCase());
                activity.setBoardLikeHistoryDTO(dto);
                allActivities.add(activity);
            }

            for (BoardFavoriteHistoryDTO dto : boardFavoriteHistories) {
                UserActivity activity = new UserActivity();
                activity.setActivityType("BoardFavorite");
                activity.setRegDate(dto.getRegDate());
                activity.setCategory(dto.boardCategory.toLowerCase());
                activity.setBoardFavoriteHistoryDTO(dto);
                allActivities.add(activity);
            }

            for (CommentLikeHistoryDTO dto : commentLikeHistories) {
                UserActivity activity = new UserActivity();
                activity.setActivityType("CommentLike");
                activity.setRegDate(dto.getRegDate());
                activity.setCategory(dto.boardCategory.toLowerCase());
                activity.setCommentLikeHistoryDTO(dto);
                allActivities.add(activity);
            }
            return allActivities;
        }
    }

}
