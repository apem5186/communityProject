package com.community.communityproject.controller;

import com.community.communityproject.dto.TokenDTO;
import com.community.communityproject.service.jwt.AuthService;
import com.community.communityproject.service.oauth.OAuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@AllArgsConstructor
public class OAuthController {

    private final OAuthService oAuthService;
    private final AuthService authService;

    @GetMapping("/kakao/callback")
    public String kakaoCallback(@RequestParam("code") String code,
                                HttpSession session, HttpServletResponse response) {
        // TODO : redirect-uri 설정해 놓은 곳 까진 가는데 이 컨트롤러가 실행이 아예 안됨
        log.info("^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^");
        log.info("^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^");

        TokenDTO tokenDTO = oAuthService.kakaoLogin(code, session);

        if (tokenDTO == null) {
            log.info("-- user 정보가 틀림 -- at login controller");
            return "login";
        }
        log.info("^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^");
        log.info("^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^");
        log.info("^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^");
        log.info("CODE : " + code);
        log.info("^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^");
        log.info("^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^");
        log.info("^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^");
        // 쿠키랑 헤더 설정
        authService.setCookie(response, tokenDTO.getAccessToken(),
                tokenDTO.getRefreshToken());

        return "redirect:/";
    }
}
