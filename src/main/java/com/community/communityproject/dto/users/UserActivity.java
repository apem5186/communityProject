package com.community.communityproject.dto.users;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class UserActivity {

    private String activityType;
    private LocalDateTime regDate;
    private String category;
    private UsersHistoryDTO.BoardHistoryDTO boardHistoryDTO;
    private UsersHistoryDTO.CommentHistoryDTO commentHistoryDTO;
    private UsersHistoryDTO.BoardLikeHistoryDTO boardLikeHistoryDTO;
    private UsersHistoryDTO.BoardFavoriteHistoryDTO boardFavoriteHistoryDTO;
    private UsersHistoryDTO.CommentLikeHistoryDTO commentLikeHistoryDTO;
}
