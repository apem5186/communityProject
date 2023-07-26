package com.community.communityproject.controller;

import com.community.communityproject.service.jwt.TokenProvider;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
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
    @GetMapping("/")
    public String root(Authentication authentication, HttpServletResponse response, Model model,
                       @CookieValue("access-token") String accessToken) {

        log.info("MAIN PAGE ACCESSTOKEN : " + accessToken);
        model.addAttribute("accessToken", accessToken);
        return "main";
    }
}
