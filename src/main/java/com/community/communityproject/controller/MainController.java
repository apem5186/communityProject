package com.community.communityproject.controller;

import com.community.communityproject.dto.TokenDTO;
import com.community.communityproject.dto.board.BoardListResponseDTO;
import com.community.communityproject.service.board.BoardService;
import com.community.communityproject.service.oauth.OAuthService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
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
import java.util.Map;

@Controller
@Slf4j
@RequiredArgsConstructor
public class MainController {

    private final OAuthService oAuthService;
    private final BoardService boardService;
    @GetMapping("/")
    public String root(HttpServletResponse response, Model model,
                       HttpServletRequest request,
                       @CookieValue(value = "access-token", required = false) String accessToken,
                       HttpSession session) {
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
            if (oAuthService.isSocialUser()) {
                // OAuth 로그인 했는데 토큰이 없다면
                Cookie[] cookies = request.getCookies();
                String at = null;
                String rt = null;
                for (Cookie c : cookies) {
                    if (c.getName().equals("refresh-token"))
                        rt = c.getValue();
                    else if (c.getName().equals("access-token"))
                        at = c.getValue();
                }
                Map<String, String> kToken = oAuthService.getToken(session);
                if (at == null || rt == null) {
                    TokenDTO tokenDTO = oAuthService.createToken(response);
                    log.info("AT : " + tokenDTO.getAccessToken());
                    log.info("RT : " + tokenDTO.getRefreshToken());
                }
                if (kToken == null) {
                    Map<String, String> kakaoToken = oAuthService.getKakaoToken(request);
                    log.info("kakao_access_token : " + kakaoToken.get("kakao_access_token"));
                    log.info("kakao_refresh_token : " + kakaoToken.get("kakao_refresh_token"));
                }
            }

            log.info("MAIN PAGE ACCESSTOKEN : " + accessToken);
            model.addAttribute("accessToken", accessToken);

            Cookie[] cookies = request.getCookies();
            String at = null;
            String rt = null;
            for (Cookie c : cookies) {
                if (c.getName().equals("refresh-token"))
                    rt = c.getValue();
                else if (c.getName().equals("access-token"))
                    at = c.getValue();
            }
            log.info("COOKIES at : " + at);
            log.info("COOKIES RT : " + rt);
        }




        return "main";
    }
}
