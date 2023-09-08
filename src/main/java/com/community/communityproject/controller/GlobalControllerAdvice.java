package com.community.communityproject.controller;

import com.community.communityproject.service.users.UserService;
import com.community.communityproject.service.util.FormatDate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

/**
 * 모든 페이지에 공통으로 model.attribute()를 추가함
 */
@Slf4j
@ControllerAdvice
@RequiredArgsConstructor
public class GlobalControllerAdvice {

    private final UserService userService;
    private final FormatDate formatDate;

    @ModelAttribute
    public void addAttributes(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && !(auth instanceof AnonymousAuthenticationToken)) {
            Long uid = userService.getUsers().getId();
            String filePath = userService.findImage(auth.getName());
            model.addAttribute("profileImage", filePath);
            model.addAttribute("uid", uid);
        } else {
            String filePath = userService.defaultImage();
            model.addAttribute("profileImage", filePath);
        }

        model.addAttribute("dateFormatter", formatDate);
    }
}
