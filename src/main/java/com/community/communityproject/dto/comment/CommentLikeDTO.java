package com.community.communityproject.dto.comment;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CommentLikeDTO {
    private String cid;
    private String bid;
    private int page;
    private String category;
}
