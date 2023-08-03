package com.community.communityproject.controller;

import com.community.communityproject.service.users.UserService;
import com.community.communityproject.service.jwt.TokenProvider;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;

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
        if (authentication.isAuthenticated()) {
            String filePath = userService.findImage(authentication.getName());
            model.addAttribute("profileImage", filePath);
            log.info("MAIN PAGE ACCESSTOKEN : " + accessToken);
            model.addAttribute("accessToken", accessToken);
        }
        return "main";
    }
}
