package com.community.communityproject.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UsersSignupDTO {

    @Size
    //@NotEmpty(message = "username is required")
    private String username;

    //@NotEmpty(message = "password is required")
    private String password1;

    //@NotEmpty(message = "password verification is mandatory")
    private String password2;

    //@NotEmpty(message = "Email is required")
    @Email
    private String email;

    private boolean usernameChecked;
}
