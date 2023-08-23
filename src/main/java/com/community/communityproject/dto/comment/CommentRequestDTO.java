package com.community.communityproject.dto.comment;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CommentRequestDTO {

    private String content;
    private String userEmail;
    private String category;
}
