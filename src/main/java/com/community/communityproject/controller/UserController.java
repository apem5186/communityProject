package com.community.communityproject.controller;

import com.community.communityproject.dto.TokenDTO;
import com.community.communityproject.dto.UsersInfo;
import com.community.communityproject.dto.UsersLoginDTO;
import com.community.communityproject.dto.UsersSignupDTO;
import com.community.communityproject.entitiy.users.Users;
import com.community.communityproject.repository.UserRepository;
import com.community.communityproject.service.UserService;
import com.community.communityproject.service.jwt.AuthService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.parameters.P;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Controller
@Slf4j
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final UserRepository userRepository;
    private final AuthService authService;

    private final int COOKIE_EXPIRATION = 60; // 90일
    private final int ATCOOKIE_EXPIRATION = 30;

    @GetMapping("/login")
    public String login(Model model) {
        model.addAttribute("usersLoginDTO", new UsersLoginDTO());
        return "login";
    }

    @PostMapping("/login")
    public String login(@ModelAttribute @Valid UsersLoginDTO usersLoginDTO, BindingResult bindingResult,
                                   HttpServletRequest request, HttpServletResponse response,
                        Model model) throws IOException {
        if (bindingResult.hasErrors()) {
            return "login";
        }
        // Users 등록 및 RT 저장
        TokenDTO tokenDTO = authService.login(usersLoginDTO);

        if (tokenDTO == null) {
            log.info("-- user 정보가 틀림 -- at login controller");
            bindingResult.rejectValue("Not found User", "usersLoginDTO", "Email 혹은 Password가 일치하지 않습니다.");
            return "login";
        }

        // RT 저장
        Cookie cookie = new Cookie("refresh-token", tokenDTO.getRefreshToken());
        cookie.setMaxAge(COOKIE_EXPIRATION);
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        response.addCookie(cookie);

        // AT 저장
        Cookie atCookie = new Cookie("access-token", tokenDTO.getAccessToken());
        atCookie.setMaxAge(ATCOOKIE_EXPIRATION);
        atCookie.setHttpOnly(true);
        atCookie.setSecure(true);
        response.addCookie(atCookie);

        response.setHeader("Authorization", "Bearer " + tokenDTO.getAccessToken());

        return "redirect:/";
    }

    @GetMapping("/signup")
    public String signup(Model model) {
        model.addAttribute("usersSignupDTO", new UsersSignupDTO());
        return "signup";
    }

    @PostMapping("/signup")
    public String signup(@Valid UsersSignupDTO usersSignupDTO, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return "/signup";
        }

        if (!usersSignupDTO.getPassword1().equals(usersSignupDTO.getPassword2())) {
            bindingResult.rejectValue("password2", "passwordInCorrect",
                    "비밀번호가 일치하지 않습니다.");
            return "/signup";
        }

        if (userRepository.existsByUsername(usersSignupDTO.getUsername())) {
            bindingResult.rejectValue("username", "error.usersSignupDTO", "Username already in use");
            return "signup";
        }

        if (!usersSignupDTO.isUsernameChecked()) {
            bindingResult.rejectValue("username", "error.usersSignupDTO", "Check Username");
            return "signup";
        }

        try {
            userService.signup(usersSignupDTO.getUsername(), usersSignupDTO.getEmail(), usersSignupDTO.getPassword1());
        } catch (DataIntegrityViolationException e) {
            e.printStackTrace();
            bindingResult.reject("signupFailed", "이미 등록된 사용자입니다.");
            return "signup";
        } catch (Exception e) {
            e.printStackTrace();
            bindingResult.reject("signupFailed", e.getMessage());
            return "signup";
        }

        return "redirect:/login";
    }

    @PostMapping("/signup/checkUsername")
    @ResponseBody
    public Map<String, Boolean> checkUsername(@RequestBody Map<String, String> username) {
        Map<String, Boolean> response = new HashMap<>();
        log.info("USERNAME : " + username);
        response.put("exists", userRepository.existsByUsername(username.get("username")));
        return response;
    }

    @GetMapping("/profile")
    public String profile(Model model, Authentication authentication) {
        model.addAttribute("user", userService.loadUser(authentication.getName()));
        return "profile";
    }

    @PostMapping("/auth/validate")
    public ResponseEntity<?> validate(@RequestHeader("Authorization") String requestAccessToken) {
        if (!authService.validate(requestAccessToken)) {
            return ResponseEntity.status(HttpStatus.OK).build();  // 재발급 필요 x
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();  // 재발급 필요
        }
    }

    @PostMapping("/auth/reissue")
    public ResponseEntity<?> reissue(@CookieValue(name = "refresh-token") String requestRefreshToken,
                                     @RequestHeader("Authorization") String requestAccessToken) {
        TokenDTO reissuedTokenDTO = authService.reissue(requestAccessToken, requestRefreshToken);

        if (reissuedTokenDTO != null) { // 토큰 재발급 성공
            // RT 저장
            ResponseCookie responseCookie = ResponseCookie.from("refresh-token", reissuedTokenDTO.getRefreshToken())
                    .maxAge(COOKIE_EXPIRATION)
                    .httpOnly(true)
                    .secure(true)
                    .build();
            return ResponseEntity
                    .status(HttpStatus.OK)
                    .header(HttpHeaders.SET_COOKIE, responseCookie.toString())
                    // AT 저장
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + reissuedTokenDTO.getAccessToken())
                    .build();
        } else {  // Refresh Token 탈취 가능성
            // Cookie 삭제 후 재로그인 유도
            ResponseCookie responseCookie = ResponseCookie.from("refresh-token", "")
                    .maxAge(0)
                    .path("/")
                    .build();
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .header(HttpHeaders.SET_COOKIE, responseCookie.toString())
                    .build();
        }
    }

    // 로그아웃
//    @PostMapping("/logout")
//    public ResponseEntity<?> logout(@CookieValue(name = "access-token") String requestAccessToken) {
//        authService.logout(requestAccessToken);
//        ResponseCookie responseCookie = ResponseCookie.from("refresh-token", "")
//                .maxAge(0)
//                .path("/")
//                .build();
//
//        return ResponseEntity
//                .status(HttpStatus.OK)
//                .header(HttpHeaders.SET_COOKIE, responseCookie.toString())
//                .build();
//    }

    @PostMapping("/logout")
    public String logout(@CookieValue(name = "access-token", required = false) String requestAccessToken,
                         @CookieValue(name = "refresh-token", required = false) String requestRefreshToken,
                         HttpServletResponse response) {
        log.info("hihiihihhi");
        if (requestAccessToken != null) {
            authService.logout(requestAccessToken);
        }

        // access-token 쿠키 삭제
        Cookie accessTokenCookie = new Cookie("access-token", "");
        accessTokenCookie.setMaxAge(0);
        accessTokenCookie.setHttpOnly(true);
        accessTokenCookie.setSecure(true);
        accessTokenCookie.setPath("/");
        response.addCookie(accessTokenCookie);

        // refresh-token 쿠키 삭제
        Cookie refreshTokenCookie = new Cookie("refresh-token", "");
        refreshTokenCookie.setMaxAge(0);
        refreshTokenCookie.setHttpOnly(true);
        refreshTokenCookie.setSecure(true);
        refreshTokenCookie.setPath("/");
        response.addCookie(refreshTokenCookie);

        return "redirect:/login";
    }

}