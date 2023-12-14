package com.community.communityproject.dto.board;

import com.community.communityproject.entity.board.Category;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PUBLIC)
public class BoardEditRequestDTO implements BoardDTOInterface{

    @NotEmpty
    private Long bid;

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

    private Set<Category> notices;

}
