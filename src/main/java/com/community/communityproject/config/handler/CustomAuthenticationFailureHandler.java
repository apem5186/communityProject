package com.community.communityproject.config.handler;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AccountExpiredException;
import org.springframework.security.authentication.CredentialsExpiredException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;

import java.io.IOException;

@Slf4j
public class CustomAuthenticationFailureHandler extends SimpleUrlAuthenticationFailureHandler {

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) throws IOException, ServletException {
        String errorMessage = "Invalid username or password";

        if (exception instanceof LockedException) {
            errorMessage = "Your account has been locked due to too many failed login attempts. Please contact support.";
        } else if (exception instanceof DisabledException) {
            errorMessage = "Your account has been disabled. Please contact support.";
        } else if (exception instanceof AccountExpiredException) {
            errorMessage = "Your account has expired. Please contact support.";
        } else if (exception instanceof CredentialsExpiredException) {
            errorMessage = "Your credentials have expired. Please login again.";
        }

        log.debug("Authentication failed with exception: {}", exception.getMessage());
        log.debug("Error message set to: {}", errorMessage);

        setDefaultFailureUrl("/login?error=true&message=" + errorMessage);
        log.info("Login Error : " + errorMessage);

        super.onAuthenticationFailure(request, response, exception);
    }
}