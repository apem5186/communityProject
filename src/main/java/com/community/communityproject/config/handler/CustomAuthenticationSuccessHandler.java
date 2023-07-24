package com.community.communityproject.config.handler;

import com.community.communityproject.dto.TokenDTO;
import com.community.communityproject.entitiy.users.Users;
import com.community.communityproject.repository.UserRepository;
import com.community.communityproject.service.jwt.AuthService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.DefaultRedirectStrategy;
import org.springframework.security.web.RedirectStrategy;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Component
@Slf4j
@RequiredArgsConstructor
public class CustomAuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final UserRepository userRepository;
    private final AuthenticationManagerBuilder authenticationManagerBuilder;
    private final AuthService authService;
    private final RedirectStrategy redirectStrategy = new DefaultRedirectStrategy();

     private final int COOKIE_EXPIRATION = 7776000; // 90일

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException {
        SecurityContextHolder.getContext().setAuthentication(authentication);
        log.info("SECURITY AUTHENTICATION : " + SecurityContextHolder.getContext().getAuthentication().getName());
        Users users = userRepository.findByUsername(authentication.getName()).orElseThrow();
//        UsernamePasswordAuthenticationToken authenticationToken =
//                new UsernamePasswordAuthenticationToken(users.getUsername(), users.getPassword());
//        authentication = authenticationManagerBuilder.getObject()
//                        .authenticate(authentication);
        TokenDTO tokenDTO = authService.generateToken("Server", authentication.getName(), authService.getAuthorities(authentication));
        Cookie cookie = new Cookie("refresh-token", tokenDTO.getRefreshToken());
        cookie.setMaxAge(COOKIE_EXPIRATION);
        cookie.setHttpOnly(true);
        cookie.setSecure(true);

        response.addCookie(cookie);
        response.setStatus(200);
        response.setHeader(HttpHeaders.SET_COOKIE, cookie.toString());
        response.setHeader(HttpHeaders.AUTHORIZATION, "Bearer " + tokenDTO.getAccessToken());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.toString());

        users.setLogin(true);
        userRepository.save(users);
        log.info("Authentication Info : " + authentication.getPrincipal().toString() + " something : " + authentication.getName());
        redirectStrategy.sendRedirect(request, response, "/");
    }
}
