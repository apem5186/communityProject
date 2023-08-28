package com.community.communityproject.dto.comment;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CommentEditDTO {

    private String cid;
    private String bid;
    private String category;
    private int page;
    private String content;
}
