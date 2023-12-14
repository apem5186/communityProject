package com.community.communityproject.config.handler;

import com.community.communityproject.dto.TokenDTO;
import com.community.communityproject.service.jwt.AuthService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class CustomOAuthSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final AuthService authService;
    private final OAuth2AuthorizedClientService authorizedClientService;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication)
        throws IOException, ServletException {

        TokenDTO tokenDTO = authService.generateToken("Server", authentication.getName(), authService.getAuthorities(authentication));
        authService.setCookie(response, tokenDTO.getAccessToken(), tokenDTO.getRefreshToken());

        OAuth2AuthenticationToken authenticationToken = (OAuth2AuthenticationToken) authentication;
        OAuth2AuthorizedClient authorizedClient = authorizedClientService.loadAuthorizedClient(
                authenticationToken.getAuthorizedClientRegistrationId(),
                authenticationToken.getName());

        String kakao_AccessToken = authorizedClient.getAccessToken().getTokenValue();
        String kakao_RefreshToken = authorizedClient.getRefreshToken().getTokenValue();
        log.info("token" + tokenDTO.getAccessToken());
        HttpSession session = request.getSession(true);
        session.setAttribute("kakao_access_token", kakao_AccessToken);
        session.setAttribute("kakao_refresh_token", kakao_RefreshToken);

        log.info("===============================");
        log.info("CustomOAuthSuccessHandler 실행");
        log.info("===============================");
        super.onAuthenticationSuccess(request, response, authentication);
    }
}
