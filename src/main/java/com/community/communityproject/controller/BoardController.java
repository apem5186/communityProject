package com.community.communityproject.controller;

import com.community.communityproject.dto.board.BoardEditRequestDTO;
import com.community.communityproject.dto.board.BoardListResponseDTO;
import com.community.communityproject.dto.board.BoardRequestDTO;
import com.community.communityproject.dto.comment.CommentListResponseDTO;
import com.community.communityproject.service.board.BoardService;
import com.community.communityproject.service.comment.CommentService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.HashSet;
import java.util.Set;

@Slf4j
@Controller
@RequiredArgsConstructor
public class BoardController {

    private final BoardService boardService;
    private final CommentService commentService;

    @GetMapping("/{path:(?:community|notice|questions|knowledge)}")
    public String list(Model model, @PathVariable String path,
                       @RequestParam(value = "page", defaultValue = "1") int page,
                       @RequestParam(value = "kw", defaultValue = "") String kw,
                       @RequestParam(value = "sort", defaultValue = "LATEST") String sort) {
        Page<BoardListResponseDTO.BoardDTO> paging = this.boardService.getBoardListDTO(page, kw, sort, path);
        model.addAttribute("category", path);
        model.addAttribute("paging", paging);
        model.addAttribute("kw", kw);
        return "board/" + path;
    }
    
    // TODO : model에 추가하는 값들 따로 메소드 만들어서 분리하기, CommentController에서 commentPost에서 써야함 service단에서 만들어야함
    @GetMapping("/{path:(?:community|notice|questions|knowledge)}/{bid}")
    public String getBoard(Model model, @PathVariable String path,
                           @RequestParam(value = "rvPage", defaultValue = "1") int page,
                           @PathVariable String bid, HttpSession session, HttpServletRequest request) {
        Long boardId = Long.valueOf(bid);
        // 세션에 저장된 조회한 게시글 ID 목록을 가져옵니다.
        Set<Long> viewedBoards = (Set<Long>) session.getAttribute("viewedBoards");
        if (viewedBoards == null) {
            viewedBoards = new HashSet<>();
        }

        // 해당 게시글을 이전에 조회하지 않았다면 조회수를 증가시킵니다.
        if (!viewedBoards.contains(boardId)) {
            boardService.updateHits(Math.toIntExact(boardId));
            viewedBoards.add(boardId);
            session.setAttribute("viewedBoards", viewedBoards);
        }
        BoardListResponseDTO.BoardDTO boardDTO = this.boardService.getBoard(Long.valueOf(bid));
        Page<CommentListResponseDTO.CommentDTO> commentDTO = this.commentService.getCommentsFromBoard(page, bid);

        String likeStatus = this.boardService.checklikeStatus(boardId);
        model.addAttribute("board", boardDTO);
        model.addAttribute("comments", commentDTO);
        // 권한이 있는 사용자만
        if (request.isUserInRole("ROLE_USER") || request.isUserInRole("ROLE_ADMIN")) {
            boolean isFavorite = this.boardService.hasFavoriteBoard(boardId);
            // 이 게시글에 추천이나 비추천을 눌렀던 사용자라면
            if (likeStatus != null) {
                // 뭘 눌렀는지 모델에 추가시킴
                model.addAttribute("likeStatus", likeStatus);
            }
            if (isFavorite) {
                model.addAttribute("isFavorite", isFavorite);
            }
        }
        return "board/boardDetail";
    }

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

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/{path:(?:community|notice|questions|knowledge)}/edit/{bid}")
    public String edit(Model model,
                       @PathVariable String path,
                       @PathVariable String bid) {
        BoardEditRequestDTO boardEditRequestDTO = boardService.setEditBoard(Long.valueOf(bid));
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        boardEditRequestDTO.setEmail(email);
        model.addAttribute("boardEditRequestDTO", boardEditRequestDTO);
        model.addAttribute("category", path);
        model.addAttribute("bid", bid);
        return "/board/boardEdit";
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/{path:(?:community|notice|questions|knowledge)}/edit/{bid}")
    public String edit(@PathVariable String path, @PathVariable String bid, Model model,
                       BoardEditRequestDTO boardEditRequestDTO, HttpServletRequest request,
                       HttpServletResponse response) {
        boardService.editBoard(request, response, boardEditRequestDTO);
        BoardListResponseDTO.BoardDTO boardDTO = this.boardService.getBoard(Long.valueOf(bid));
        model.addAttribute("board", boardDTO);
        return String.format("redirect:/%s/%s", boardDTO.getCategory().toLowerCase(), bid);
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/{path:(?:community|notice|questions|knowledge)}/delete/{bid}")
    public String delete(@PathVariable String path, @PathVariable String bid, Model model,
                         HttpServletRequest request, HttpServletResponse response,
                         @RequestParam String id) {
        boardService.deleteBoard(request, response, Long.valueOf(id));
        return String.format("redirect:/%s", path);
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/{path:(?:community|notice|questions|knowledge)}/like/{bid}")
    public String likeBoard(@PathVariable String path, @PathVariable String bid,
                            HttpServletRequest request, HttpServletResponse response) {
        boardService.likeBoard(Long.valueOf(bid), response, request, true);
        return String.format("redirect:/%s/%s", path, bid);
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/{path:(?:community|notice|questions|knowledge)}/favorite/{bid}")
    public String favoriteBoard(@PathVariable String path, @PathVariable String bid,
                                HttpServletRequest request, HttpServletResponse response) {
        boardService.favoriteBoard(Long.valueOf(bid), response, request);
        return String.format("redirect:/%s/%s", path, bid);
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/{path:(?:community|notice|questions|knowledge)}/dislike/{bid}")
    public String disLikeBoard(@PathVariable String path, @PathVariable String bid,
                            HttpServletRequest request, HttpServletResponse response) {
        boardService.likeBoard(Long.valueOf(bid), response, request, false);
        return String.format("redirect:/%s/%s", path, bid);
    }
}
