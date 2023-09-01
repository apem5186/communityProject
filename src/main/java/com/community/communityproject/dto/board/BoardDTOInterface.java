package com.community.communityproject.dto.board;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface BoardDTOInterface {
    String getTitle();
    String getContent();
    String getCategory();
    List<MultipartFile> getBoardImage();
    String getEmail();
}
