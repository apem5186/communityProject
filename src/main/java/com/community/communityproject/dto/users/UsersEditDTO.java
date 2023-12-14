package com.community.communityproject.dto.users;

import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
public class UsersEditDTO {

    private String username;
    private String email;
    private String password;
    private MultipartFile profileImage;
}
