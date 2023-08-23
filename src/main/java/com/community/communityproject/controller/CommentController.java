package com.community.communityproject.controller;

import com.community.communityproject.dto.comment.CommentRequestDTO;
import com.community.communityproject.service.comment.CommentService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Slf4j
@Controller
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    @PostMapping("/post/comment")
    public String commentPost(@Valid CommentRequestDTO commentRequestDTO, BindingResult bindingResult,
            Model model, HttpServletRequest request, HttpServletResponse response,
            @RequestParam(value = "bid") String bid) {
        commentService.PostComment(request, response, commentRequestDTO, bid);
        return String.format("redirect:/%s/%s", commentRequestDTO.getCategory(), bid);
    }
}
