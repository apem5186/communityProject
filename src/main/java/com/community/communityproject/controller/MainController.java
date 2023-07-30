package com.community.communityproject.controller;

import com.community.communityproject.entitiy.users.ProfileImage;
import com.community.communityproject.entitiy.users.Users;
import com.community.communityproject.service.UserService;
import com.community.communityproject.service.jwt.TokenProvider;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Controller
@Slf4j
@RequiredArgsConstructor
public class MainController {

    private final TokenProvider tokenProvider;
    private final UserService userService;
    @GetMapping("/")
    public String root(HttpServletResponse response, Model model,
                       @CookieValue(value = "access-token", required = false) String accessToken) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        ProfileImage profileImage = userService.getProfileImage(authentication.getName());
        model.addAttribute("profileImage", profileImage);
        log.info("MAIN PAGE ACCESSTOKEN : " + accessToken);
        model.addAttribute("accessToken", accessToken);
        return "main";
    }
}
