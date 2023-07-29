package com.community.communityproject.service;

import com.community.communityproject.dto.TokenDTO;
import com.community.communityproject.dto.UsersEditDTO;
import com.community.communityproject.dto.UsersInfo;
import com.community.communityproject.entitiy.users.UserRole;
import com.community.communityproject.entitiy.users.Users;
import com.community.communityproject.repository.UserRepository;
import com.community.communityproject.service.jwt.AuthService;
import com.community.communityproject.service.jwt.TokenProvider;
import com.community.communityproject.service.redis.RedisService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;

    private final AuthService authService;

    private final UserSecurityService userSecurityService;

    private final TokenProvider tokenProvider;

    private final RedisService redisService;

    private int RTCOOKIE_EXPIRATION;
    private int ATCOOKIE_EXPIRATION;

    private final String SERVER = "Server";

    @Autowired
    public void setCookieExpiration(@Value("${jwt.refresh-token-validity-in-seconds}") int cookieExpiration) {
        this.RTCOOKIE_EXPIRATION = cookieExpiration;
    }

    @Autowired
    public void setAtCookieExpiration(@Value("${jwt.access-token-validity-in-seconds}") int atCookieExpiration) {
        this.ATCOOKIE_EXPIRATION = atCookieExpiration;
    }

    @Transactional
    public void signup(String username, String email, String password) {
        Users users = new Users();
        users.setUsername(username);
        users.setEmail(email);
        users.setPassword(passwordEncoder.encode(password));
        users.setUserRole(UserRole.USER);
        userRepository.save(users);
    }

    public UsersInfo loadUser(String email) {
        Users users = userRepository.findByEmail(email).orElseThrow();
        UsersInfo usersInfo = UsersInfo.builder()
                .username(users.getUsername())
                .email(users.getEmail())
                .userRole(users.getUserRole())
                .build();
        return usersInfo;
    }

    public UsersEditDTO loadEditUserInfo(String email) {
        Users users = userRepository.findByEmail(email).orElseThrow();
        UsersEditDTO usersEditDTO = new UsersEditDTO();
        usersEditDTO.setEmail(users.getEmail());
        usersEditDTO.setUsername(users.getUsername());
        return usersEditDTO;
    }

    public String editUserCheck(UsersEditDTO usersEditDTO, String beforeEmail) {
        Users users = userRepository.findByEmail(beforeEmail).orElseThrow();
        // 기존 이메일이 아닌데 바꿀려는 이메일이 이미 존재하면
        if (!users.getEmail().equals(usersEditDTO.getEmail()) && userRepository.existsByEmail(usersEditDTO.getEmail())) {
            return "emailError";
        }
        // 기존 유저네임이 아닌데 바꿀려는 유저네임이 이미 존재하면
        if (!users.getUsername().equals(usersEditDTO.getUsername()) && userRepository.existsByUsername(usersEditDTO.getUsername())) {
            return "usernameError";
        }

        return "ok";
    }

    @Transactional
    public void editUser(UsersEditDTO usersEditDTO, String beforeEmail, HttpServletRequest request,
                         HttpServletResponse response) {

        Cookie[] cookies = request.getCookies();
        String at = null;
        String rt = null;
        for (Cookie c : cookies) {
            if (c.getName().equals("refresh-token"))
                rt = c.getValue();
            else if (c.getName().equals("access-token"))
                at = c.getValue();
        }


        if (at != null && rt != null) {
            log.info("EDIT USER INFO START AT : " + at);
            log.info("RT : " + rt);
            Users users = userRepository.findByEmail(beforeEmail).orElseThrow();
            users.setEmail(usersEditDTO.getEmail());
            users.setUsername(usersEditDTO.getUsername());
            users.setPassword(passwordEncoder.encode(usersEditDTO.getPassword()));
            users.setUserRole(UserRole.USER);

            userRepository.save(users);

            TokenDTO tokenDTO = authService.reissue(at, rt);

            // RT 저장
            Cookie cookie = new Cookie("refresh-token", tokenDTO.getRefreshToken());
            cookie.setMaxAge(RTCOOKIE_EXPIRATION);
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
        }
    }

    @Transactional
    public String deleteUsers(HttpServletRequest request, HttpServletResponse response) {
        Cookie[] cookies = request.getCookies();
        String rt = null;
        String at = null;
        for (Cookie c : cookies) {
            if (c.getName().equals("refresh-token"))
                rt = c.getValue();
            else if (c.getName().equals("access-token")) {
                at = c.getValue();
            }
        }
        String email = null;


        // authService.validate가 true를 반환하면 at 만료일자 초과로 재발급
        if (authService.validate(at)) {
            TokenDTO tokenDTO = authService.reissue(at, rt);
            // tokenDTO가 null이면 rt가 없거나 탈취가능성 있음
            if (tokenDTO == null) {
                authService.logout(at, "logout");
                return null;
            }
        } else {
            email = authService.getPrincipal(at);
        }

        String refreshTokenInRedis = redisService.getValues("RT(" + SERVER + "):" + email);
        if (refreshTokenInRedis == null) { // Redis에 저장되어 있는 RT가 없을 경우
            return null; // -> 재로그인 요청
        }
        // 요청된 RT의 유효성 검사 & Redis에 저장되어 있는 RT와 같은지 비교
        if(!tokenProvider.validateRefreshToken(rt) || !refreshTokenInRedis.equals(rt)) {
            redisService.deleteValues("RT(" + SERVER + "):" + email); // 탈취 가능성 -> 삭제
            return null; // -> 재로그인 요청
        }
        Users users = userRepository.findByEmail(email).orElseThrow();
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

        authService.logout(at, "delete");
        userRepository.delete(users);

        return "ok";
    }

}
