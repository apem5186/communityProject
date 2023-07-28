package com.community.communityproject.service;

import com.community.communityproject.dto.TokenDTO;
import com.community.communityproject.dto.UsersEditDTO;
import com.community.communityproject.dto.UsersInfo;
import com.community.communityproject.entitiy.users.UserRole;
import com.community.communityproject.entitiy.users.Users;
import com.community.communityproject.repository.UserRepository;
import com.community.communityproject.service.jwt.AuthService;
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

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;

    private final AuthService authService;

    private final UserSecurityService userSecurityService;

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




            // Update the SecurityContext with the modified user's information
//            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
//            UserDetails updatedUserDetails = userSecurityService.loadUserByUsername(usersEditDTO.getEmail());
//
//            // Create a new Authentication object with the updated user details
//            Authentication newAuthentication = new UsernamePasswordAuthenticationToken(
//                updatedUserDetails,
//                authentication.getCredentials(),
//                updatedUserDetails.getAuthorities()
//            );
//
//            // Set the new Authentication object in the SecurityContextHolder
//            SecurityContextHolder.getContext().setAuthentication(newAuthentication);

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

}
