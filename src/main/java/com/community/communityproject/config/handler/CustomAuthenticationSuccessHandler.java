package com.community.communityproject.config.handler;

import com.community.communityproject.entitiy.users.Users;
import com.community.communityproject.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@Slf4j
@RequiredArgsConstructor
public class CustomAuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final UserRepository userRepository;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException {
        log.info("1=======================================");
        SecurityContextHolder.getContext().setAuthentication(authentication);
        log.info("2=======================================");
        Users users = userRepository.findByUsername(authentication.getName()).orElseThrow();
        log.info("3=======================================");
        users.setLogin(true);
        userRepository.save(users);
        log.info("Authentication Info : " + authentication.getAuthorities());
        response.sendRedirect("/");
    }
}
