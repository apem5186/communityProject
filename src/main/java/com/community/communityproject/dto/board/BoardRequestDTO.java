package com.community.communityproject.dto.board;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PUBLIC)
public class BoardRequestDTO implements BoardDTOInterface{

    @NotEmpty
    private String title;

    @NotEmpty
    private String content;

    @NotEmpty
    private String category;

    private List<MultipartFile> boardImage;

    @NotEmpty
    @Email
    private String email;
}
