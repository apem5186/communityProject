package com.community.communityproject.dto.board.admin;

import com.community.communityproject.entity.board.Board;
import com.community.communityproject.entity.board.Category;
import com.community.communityproject.entity.users.UserRole;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;

@Getter
@Setter
public class BoardDTOForAdmin {
    private Long bid;
    private String title;
    private String content;
    private int hits;
    private int reviewCnt;
    private int favoriteCnt;
    private int likeCnt;
    private Set<Category> notices;
    private Category category;
    private Long uid;
    private String username;
    private String email;
    private UserRole userRole;
    @Builder
    private BoardDTOForAdmin(Board board) {
        this.bid = board.getId();
        this.title = board.getTitle();
        this.content = board.getContent();
        this.hits = board.getHits();
        this.reviewCnt = board.getReviewCnt();
        this.favoriteCnt = board.getFavoriteCnt();
        this.likeCnt = board.getLikeCnt();
        this.notices = board.getNotices();
        this.category = board.getCategory();
        this.uid = board.getUsers().getId();
        this.username = board.getUsers().getUsername();
        this.email = board.getUsers().getEmail();
        this.userRole = board.getUsers().getUserRole();
    }
}
