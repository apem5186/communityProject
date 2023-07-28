package com.community.communityproject.dto;

import com.community.communityproject.entitiy.users.UserRole;
import lombok.Builder;
import lombok.Data;
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
