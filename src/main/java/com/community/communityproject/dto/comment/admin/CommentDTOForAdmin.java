package com.community.communityproject.dto.comment.admin;

import com.community.communityproject.entity.comment.Comment;
import com.community.communityproject.entity.users.UserRole;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Setter
@Getter
public class CommentDTOForAdmin {
    private Long cid;
    private Long bid;
    private Long uid;
    private String username;
    private String content;
    private String category;
    private int likeCnt;
    private boolean isDeleted;
    private List<CommentDTOForAdmin> children;
    private Long parent;
    private UserRole userRole;
    private LocalDateTime regDate;
    private LocalDateTime modDate;

    @Builder
    public CommentDTOForAdmin(Comment comment) {
        this.cid = comment.getId();
        this.bid = comment.getBoard().getId();
        this.uid = comment.getUsers().getId();
        this.username = comment.getUsers().getUsername();
        this.content = comment.getContent();
        this.category = comment.getBoard().getCategory().toString();
        this.likeCnt = comment.getLikeCnt();
        this.isDeleted = comment.isDeleted();
        setChildrenFromEntities(comment.getChildren());
        if (comment.getParent() == null) {
            this.parent = null;
        } else {
            this.parent = comment.getParent().getId();
        }
        this.userRole = comment.getUsers().getUserRole();
        regDate = comment.getRegDate();
        modDate = comment.getModDate();
    }

    public void setChildrenFromEntities(Set<Comment> children) {
        this.children = children.stream()
                .sorted(Comparator.comparing(Comment::getRegDate))
                .map(CommentDTOForAdmin::new)
                .collect(Collectors.toList());
    }

    public CommentDTOForAdmin getCommentDTOForAdmin(Comment comment) {
        return new CommentDTOForAdmin(comment);}
}
