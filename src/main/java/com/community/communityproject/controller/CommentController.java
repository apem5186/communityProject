package com.community.communityproject.controller;

import com.community.communityproject.dto.comment.CommentEditDTO;
import com.community.communityproject.dto.comment.CommentLikeDTO;
import com.community.communityproject.dto.comment.CommentRequestDTO;
import com.community.communityproject.service.board.BoardService;
import com.community.communityproject.service.comment.CommentService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Slf4j
@Controller
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;
    private final BoardService boardService;

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/post/comment")
    public String commentPost(@Valid CommentRequestDTO commentRequestDTO, BindingResult bindingResult,
            RedirectAttributes redirectAttributes,
            HttpServletRequest request, HttpServletResponse response,
            @RequestParam(value = "bid") String bid) {
        if (bindingResult.hasErrors()) {
            FieldError fieldError = bindingResult.getFieldError("content");
            if (fieldError != null && fieldError.getDefaultMessage() != null) {
                redirectAttributes.addFlashAttribute("emptyContent", fieldError.getDefaultMessage()); // 세션에 임시로 에러 메시지 저장
            }
            return String.format("redirect:/%s/%s", commentRequestDTO.getCategory(), bid);
        }

        commentService.PostComment(request, response, commentRequestDTO, bid);
        return String.format("redirect:/%s/%s", commentRequestDTO.getCategory(), bid);
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/edit/comment")
    public String editComment(CommentEditDTO commentEditDTO,
                              HttpServletResponse response, HttpServletRequest request,
                              Model model) {
        commentService.editComment(request, response, commentEditDTO);
        model = boardService.populateBoardModel(model, commentEditDTO.getBid(), request, commentEditDTO.getPage());
        return String.format("redirect:/%s/%s", commentEditDTO.getCategory().toLowerCase(), commentEditDTO.getBid());
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/delete/comment")
    public String deleteComment(CommentLikeDTO commentLikeDTO, // 받아야 하는 필드가 똑같아서 그냥 씀
                                HttpServletResponse response, HttpServletRequest request,
                                Model model) {
        commentService.deleteComment(request, response, commentLikeDTO.getCid(), commentLikeDTO.getBid());
        model = boardService.populateBoardModel(model, commentLikeDTO.getBid(), request, commentLikeDTO.getPage());
        return String.format("redirect:/%s/%s", commentLikeDTO.getCategory().toLowerCase(), commentLikeDTO.getBid());
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/like/comment")
    public String likeComment(CommentLikeDTO commentLikeDTO,
                              HttpServletRequest request, HttpServletResponse response,
                              Model model) {
        commentService.likeComment(Long.valueOf(commentLikeDTO.getCid()), response, request, true);
        model = boardService.populateBoardModel(model, String.valueOf(commentLikeDTO.getBid()), request, commentLikeDTO.getPage());
        return String.format("redirect:/%s/%s", commentLikeDTO.getCategory().toLowerCase(), commentLikeDTO.getBid());
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/dislike/comment")
    public String dislikeComment(CommentLikeDTO commentLikeDTO,
                                 HttpServletRequest request, HttpServletResponse response,
                                 Model model) {
        commentService.likeComment(Long.valueOf(commentLikeDTO.getCid()), response, request, false);
        model = boardService.populateBoardModel(model, String.valueOf(commentLikeDTO.getBid()), request, commentLikeDTO.getPage());
        return String.format("redirect:/%s/%s", commentLikeDTO.getCategory().toLowerCase(), commentLikeDTO.getBid());
    }

}
