package com.community.communityproject.dto.comment;

import jakarta.validation.constraints.NotEmpty;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CommentRequestDTO {

    @NotEmpty(message = "내용을 추가해 주세요.")
    private String content;
    private String userEmail;
    private String category;
}
