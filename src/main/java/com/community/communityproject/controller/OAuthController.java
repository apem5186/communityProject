package com.community.communityproject.controller;

import com.community.communityproject.service.oauth.OAuthService;
import jakarta.servlet.http.HttpSession;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;

@Slf4j
@Controller
@AllArgsConstructor
public class OAuthController {

    private final OAuthService oAuthService;

    @ResponseBody
    @GetMapping("/test/oauth/login")
    public String testOAuthLogin(
            Authentication authentication,
            @AuthenticationPrincipal OAuth2User oauth
            ) {
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();

        log.info("authentication: " + oAuth2User.getAttributes());
        log.info("OAuth2User: " + oauth.getAttributes());

        return "OAuth 세션 정보 확인";
    }

    @ResponseBody
    @GetMapping("/kakao/getToken")
    public String kakaoGetToken(HttpSession session) {
        LinkedHashMap<String, String> map = oAuthService.getToken(session);

        return String.format("at : %s\n rt : %s", map.get("kakao_access_token"), map.get("kakao_refresh_token"));
    }
}
