package com.community.communityproject.dto.users.admin;

import com.community.communityproject.entity.users.Users;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UsersDTOForAdmin {
    private Long uid;
    private String username;
    private String email;
    private String userRole;
    private boolean isLogin;
    private String profileImage;

    @Builder
    public UsersDTOForAdmin(Users users) {
        this.uid = users.getId();
        this.username = users.getUsername();
        this.email = users.getEmail();
        this.userRole = users.getUserRole().toString();
        this.isLogin = users.isLogin();
        this.profileImage = users.getProfileImage().getFilePath();
    }
}
