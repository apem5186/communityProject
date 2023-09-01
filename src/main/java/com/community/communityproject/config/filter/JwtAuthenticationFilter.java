package com.community.communityproject.config.filter;

import com.community.communityproject.service.users.UserSecurityService;
import com.community.communityproject.service.jwt.TokenProvider;
import io.jsonwebtoken.IncorrectClaimException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * 출처 : https://velog.io/@u-nij/JWT-JWT-%EA%B5%AC%ED%98%84%ED%95%98%EA%B8%B0-3-JwtAuthenticationFilter-JwtAccessDeniedHandler-JwtAuthenticationEntryPoint
 */

@Slf4j
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final TokenProvider tokenProvider;
    private final UserSecurityService userSecurityService;

    // profileImage 루트 필터검사 안함
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        String path = request.getRequestURI();
        if (path.startsWith("/profileImage/")) {
            return true;
        }
        return super.shouldNotFilter(request);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String requestUrl = request.getRequestURI();
        String requestMethod = request.getMethod();
        log.info("Request URL: " + requestUrl);
        log.info("Request Method: " + requestMethod);

        if ("GET".equals(requestMethod) && "/login".equals(requestUrl)) {
            filterChain.doFilter(request, response);
            return;
        }

        String accessToken = resolveToken(request);
        log.info("Access Token: " + accessToken);

        try {
            // 정상 토큰인지 검사
            if (accessToken != null && tokenProvider.validateAccessToken(accessToken)) {
                Authentication authentication = tokenProvider.getAuthentication(accessToken);
                UserDetails userDetails = userSecurityService.loadUserByUsername(authentication.getName());
                UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(userDetails, null,
                        userDetails.getAuthorities());
                authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authenticationToken);
                log.info("정상 유저");
                log.debug("Save authentication in SecurityContextHolder.");
            } else {
                log.debug("Invalid or missing access token.");
            }

        } catch (IncorrectClaimException e) {
            SecurityContextHolder.clearContext();
            log.debug("Invalid JWT token.");
            response.sendError(403);
        } catch (UsernameNotFoundException e) {
            SecurityContextHolder.clearContext();
            log.debug("Can't find user.");
            response.sendError(403);
        }

        filterChain.doFilter(request, response);
    }

    // HTTP Request 헤더로부터 토큰 추출
// request엔 cookie에 access token만
// response엔 header에 Authorization과 Cookie에 refresh-token
    public String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");

        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        } else {
            Cookie[] cookies = request.getCookies();
            if (cookies != null) {
                for (Cookie cookie : cookies) {
                    log.info("COOKIE NAME : " + cookie.getName());
                    if (cookie.getName().equals("access-token"))
                        return cookie.getValue();
                }
            }
        }
        return null;
    }
}
