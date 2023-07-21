package com.community.communityproject.dto;

import com.community.communityproject.entitiy.users.UserRole;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UsersInfo {

    private String username;
    private String email;
    private UserRole userRole;
}
