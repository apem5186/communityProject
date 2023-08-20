package com.community.communityproject.dto.users;

import com.community.communityproject.entity.users.UserRole;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class UsersInfo {

    private String username;
    private String email;
    private UserRole userRole;
}
