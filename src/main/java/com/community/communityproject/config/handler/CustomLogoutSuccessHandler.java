package com.community.communityproject.config.handler;

import com.community.communityproject.entitiy.users.Users;
import com.community.communityproject.repository.UserRepository;
import com.community.communityproject.service.jwt.AuthService;
import com.community.communityproject.service.redis.RedisService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.SimpleUrlLogoutSuccessHandler;

import java.io.IOException;

@Slf4j
@RequiredArgsConstructor
public class CustomLogoutSuccessHandler extends SimpleUrlLogoutSuccessHandler {

    private final UserRepository userRepository;
    private final AuthService authService;
    private final RedisService redisService;

    @Override
    public void onLogoutSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        String requestAccessToken = request.getHeader("Authorization");
        authService.logout(requestAccessToken);

        log.info("User logged out successfully.");
        Users users = userRepository.findByUsername(authentication.getName()).orElseThrow();
        users.setLogin(false);
        userRepository.save(users);

        log.info("USER ID : " + users.getId());
        super.onLogoutSuccess(request, response, authentication);
    }
}
