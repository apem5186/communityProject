package com.community.communityproject.dto.board;

import com.community.communityproject.entity.board.Category;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Set;

public interface BoardDTOInterface {
    String getTitle();
    String getContent();
    String getCategory();
    List<MultipartFile> getBoardImage();
    String getEmail();
    Set<Category> getNotices();
}
