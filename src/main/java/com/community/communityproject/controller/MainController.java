package com.community.communityproject.controller;

import com.community.communityproject.dto.board.BoardListResponseDTO;
import com.community.communityproject.service.board.BoardService;
import com.community.communityproject.service.users.UserService;
import com.community.communityproject.service.jwt.TokenProvider;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
@Slf4j
@RequiredArgsConstructor
public class MainController {

    private final TokenProvider tokenProvider;
    private final UserService userService;
    private final BoardService boardService;
    @GetMapping("/")
    public String root(HttpServletResponse response, Model model,
                       @CookieValue(value = "access-token", required = false) String accessToken) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        List<BoardListResponseDTO.BoardDTO> boardCommunity = boardService.getBoardListDTO5("community");
        List<BoardListResponseDTO.BoardDTO> boardNotice = boardService.getBoardListDTO5("notice");
        List<BoardListResponseDTO.BoardDTO> boardKnowledge = boardService.getBoardListDTO5("knowledge");
        List<BoardListResponseDTO.BoardDTO> boardQuestions = boardService.getBoardListDTO5("questions");

        model.addAttribute("community", boardCommunity);
        model.addAttribute("notice", boardNotice);
        model.addAttribute("knowledge", boardKnowledge);
        model.addAttribute("questions", boardQuestions);

        if (auth != null && !(auth instanceof AnonymousAuthenticationToken)) {
            log.info("MAIN PAGE ACCESSTOKEN : " + accessToken);
            model.addAttribute("accessToken", accessToken);
        }
        return "main";
    }
}
