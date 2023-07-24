package com.community.communityproject.controller;

import com.community.communityproject.dto.TokenDTO;
import com.community.communityproject.dto.UsersInfo;
import com.community.communityproject.dto.UsersSignupDTO;
import com.community.communityproject.entitiy.users.Users;
import com.community.communityproject.repository.UserRepository;
import com.community.communityproject.service.UserService;
import com.community.communityproject.service.jwt.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Controller
@Slf4j
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final UserRepository userRepository;
    private final AuthService authService;

    private final long COOKIE_EXPIRATION = 7776000; // 90일

    @GetMapping("/login")
    public String login() {
        return "login";
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
    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestHeader("Authorization") String requestAccessToken) {
        authService.logout(requestAccessToken);
        ResponseCookie responseCookie = ResponseCookie.from("refresh-token", "")
                .maxAge(0)
                .path("/")
                .build();

        return ResponseEntity
                .status(HttpStatus.OK)
                .header(HttpHeaders.SET_COOKIE, responseCookie.toString())
                .build();
    }

}