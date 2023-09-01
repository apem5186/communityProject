package com.community.communityproject.config.handler;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;

public class CustomLogoutHandler extends SecurityContextLogoutHandler {

    @Override
    public void logout(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
        // Access user information provided by LogoutPreProcessingFilter
        Object userInfo = request.getAttribute("user_info");
        if (userInfo != null) {
            // Perform your pre-logout action here
            // For example, logging the user information
            System.out.println("User information: " + userInfo);
        } else {
            System.out.println("USERINFO IS NULL");
        }

        super.logout(request, response, authentication);
    }
}
