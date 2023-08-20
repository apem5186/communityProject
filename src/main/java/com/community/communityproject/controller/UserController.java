package com.community.communityproject.controller;

import com.community.communityproject.dto.*;
import com.community.communityproject.dto.board.BoardListResponseDTO;
import com.community.communityproject.dto.users.UsersEditDTO;
import com.community.communityproject.dto.users.UsersLoginDTO;
import com.community.communityproject.dto.users.UsersSignupDTO;
import com.community.communityproject.repository.UserRepository;
import com.community.communityproject.service.board.BoardService;
import com.community.communityproject.service.users.UserSecurityService;
import com.community.communityproject.service.users.UserService;
import com.community.communityproject.service.jwt.AuthService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.util.UriComponentsBuilder;

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
    private final UserSecurityService userSecurityService;
    private final BoardService boardService;

    private int RTCOOKIE_EXPIRATION;
    private int ATCOOKIE_EXPIRATION;

    @Autowired
    public void setCookieExpiration(@Value("${jwt.refresh-token-validity-in-seconds}") int cookieExpiration) {
        this.RTCOOKIE_EXPIRATION = cookieExpiration;
    }

    @Autowired
    public void setAtCookieExpiration(@Value("${jwt.access-token-validity-in-seconds}") int atCookieExpiration) {
        this.ATCOOKIE_EXPIRATION = atCookieExpiration;
    }



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
        // 쿠키랑 헤더 설정
        authService.setCookie(response, tokenDTO.getAccessToken(),
                tokenDTO.getRefreshToken());

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
            userService.signup(usersSignupDTO);
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
    public String profile(@RequestParam(value = "edit", required = false) String editMode, Model model, Authentication authentication) {
        //String profileUrl = userService.findImage(authentication.getName());
        model.addAttribute("user", userService.loadUser(authentication.getName()));
        //model.addAttribute("profileImage", profileUrl);
        if ("true".equals(editMode)) {
            model.addAttribute("editingEnabled", true);
        } else {
            model.addAttribute("editingEnabled", false);
        }

        String currentUserEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        UsersEditDTO usersEditDTO = userService.loadEditUserInfo(currentUserEmail);

        model.addAttribute("usersEditDTO", usersEditDTO);


        return "profile/profile";
    }

    @PostMapping("/profile")
    public String editProfile(@Valid UsersEditDTO usersEditDTO, BindingResult bindingResult,
                              HttpServletRequest request, HttpServletResponse response,
                              RedirectAttributes redirectAttributes) {

        String beforeUserEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        // redirect 할 때 param 추가
        String redirectUrl = UriComponentsBuilder.fromPath("/profile")
                .queryParam("edit", "true")
                .toUriString();

        // Check if usersEditDTO is null
        if (usersEditDTO == null) {
            // Handle the case when usersEditDTO is null (e.g., log an error or return an error page)
            // For simplicity, you can return "profile" to stay on the profile page
            // Add the "edit=true" parameter to the redirect URL
            log.info("USERS EDIT IS NULL");
            return "redirect:/profile" + redirectAttributes;
        }

        // 중복 체크
        String dupleCheck = userService.editUserCheck(usersEditDTO, beforeUserEmail);
        log.info("DUPLE CHECK : " + dupleCheck);
        if (dupleCheck.equals("usernameError")) {
            redirectAttributes.addAttribute("edit", "true");
            redirectAttributes.addAttribute("usernameError", "That Username already exists.");
            return "redirect:/profile";
        } else if (dupleCheck.equals("emailError")) {
            redirectAttributes.addAttribute("edit", "true");
            redirectAttributes.addAttribute("emailError", "That Email already exists.");
            return "redirect:/profile";
        }

        if (bindingResult.hasErrors()) {
            // Add the "edit=true" parameter to the redirect URL
            log.info("SOMETHING IS WRONG");
            log.error("ERROR : " + bindingResult.getFieldError());
            return "redirect:" + redirectUrl;
        }

        userService.editUser(usersEditDTO, beforeUserEmail, request, response);



        return "redirect:/profile";
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/profile/posts")
    public String profileBoards(HttpServletRequest request, HttpServletResponse response,
                                Model model,
                                @RequestParam(value = "page", defaultValue = "1") int page,
                                @RequestParam(value = "username") String username,
                                @RequestParam(value = "email") String email) {

        Page<BoardListResponseDTO.BoardDTO> board = boardService.getMyBoardListDTO(page, response, request);
        model.addAttribute("paging", board);

        UsersEditDTO usersEditDTO = userService.getUsersInfo(email, username);
        model.addAttribute("user", usersEditDTO);

        return "profile/profileMyBoard";
    }


    @PreAuthorize("isAuthenticated()")
    @GetMapping("/profile/favorites")
    public String profileFavorites(HttpServletRequest request, HttpServletResponse response,
                                Model model,
                                @RequestParam(value = "page", defaultValue = "1") int page,
                                @RequestParam(value = "username") String username,
                                @RequestParam(value = "email") String email) {
        Page<BoardListResponseDTO.BoardDTO> board = boardService.getMyFavoriteListDTO(page, response, request);
        model.addAttribute("paging", board);

        UsersEditDTO usersEditDTO = userService.getUsersInfo(email, username);
        model.addAttribute("user", usersEditDTO);

        return "profile/profileMyFavorite";
    }

    @PostMapping("/delete/users")
    public String deleteUsers(HttpServletRequest request, HttpServletResponse response,
                              RedirectAttributes redirectAttributes) {
        String result = userService.deleteUsers(request, response);
        if (result == null) {
            redirectAttributes.addAttribute("error", "deleteError");
            return "redirect:/login";
        }
        return "redirect:/";
    }
//    @PostMapping("/auth/validate")
//    public ResponseEntity<?> validate(@RequestHeader("Authorization") String requestAccessToken) {
//        if (!authService.validate(requestAccessToken)) {
//            return ResponseEntity.status(HttpStatus.OK).build();  // 재발급 필요 x
//        } else {
//            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();  // 재발급 필요
//        }
//    }

    @PostMapping("/auth/reissue")
    public ResponseEntity<?> reissue(@CookieValue(name = "refresh-token") String requestRefreshToken,
                                     @RequestHeader("Authorization") String requestAccessToken) {
        TokenDTO reissuedTokenDTO = authService.reissue(requestAccessToken, requestRefreshToken);

        if (reissuedTokenDTO != null) { // 토큰 재발급 성공
            // RT 저장
            ResponseCookie responseCookie = ResponseCookie.from("refresh-token", reissuedTokenDTO.getRefreshToken())
                    .maxAge(RTCOOKIE_EXPIRATION)
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

    @PostMapping("/logout")
    public String logout(@CookieValue(name = "access-token", required = false) String requestAccessToken,
                         @CookieValue(name = "refresh-token", required = false) String requestRefreshToken,
                         HttpServletResponse response) {
        log.info("hihiihihhi");
        if (requestAccessToken != null) {
            authService.logout(requestAccessToken, "logout");
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