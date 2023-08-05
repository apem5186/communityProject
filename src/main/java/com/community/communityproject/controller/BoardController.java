package com.community.communityproject.controller;

import com.community.communityproject.dto.board.BoardRequestDTO;
import com.community.communityproject.service.board.BoardService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Slf4j
@Controller
@RequiredArgsConstructor
public class BoardController {

    private final BoardService boardService;

//    @GetMapping("/community")
//    public List<>

    @GetMapping("/{path:(?:community|notice|questions|knowledge)}/new")
    public String post(Model model,
                       @PathVariable String path) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        BoardRequestDTO boardRequestDTO = new BoardRequestDTO();
        boardRequestDTO.setCategory(path);
        boardRequestDTO.setEmail(email);
        model.addAttribute("boardRequestDTO", boardRequestDTO);
        model.addAttribute("category", path);
        return "board/post";
    }

    @PostMapping("/{path:(?:community|notice|questions|knowledge)}/new")
    public String post(@Valid BoardRequestDTO boardRequestDTO, BindingResult bindingResult,
                       HttpServletResponse response, HttpServletRequest request,
                       @PathVariable String path) {
        if (bindingResult.hasErrors()) {
            bindingResult.getAllErrors().forEach(error -> {
                System.out.println(error.getDefaultMessage());
            });}
            boardService.postBoard(response, request, boardRequestDTO);
            return "redirect:/" + path;

    }
}
