package com.community.communityproject.controller;

import com.community.communityproject.dto.board.BoardEditRequestDTO;
import com.community.communityproject.dto.board.BoardListResponseDTO;
import com.community.communityproject.dto.board.BoardRequestDTO;
import com.community.communityproject.service.board.BoardService;
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

@Slf4j
@Controller
@RequiredArgsConstructor
public class BoardController {

    private final BoardService boardService;

    @GetMapping("/{path:(?:community|notice|questions|knowledge)}")
    public String list(Model model, @PathVariable String path,
                       @RequestParam(value = "page", defaultValue = "1") int page,
                       @RequestParam(value = "kw", defaultValue = "") String kw,
                       @RequestParam(value = "sort", defaultValue = "LATEST") String sort) {
        Page<BoardListResponseDTO.BoardDTO> paging = this.boardService.getBoardListDTO(page, kw, sort, path);
        Page<BoardListResponseDTO.BoardDTO> notices = this.boardService.getNoticeBoardListDTO(page, path);
        model.addAttribute("notices", notices);
        model.addAttribute("category", path);
        model.addAttribute("paging", paging);
        model.addAttribute("kw", kw);
        return "board/" + path;
    }
    @GetMapping("/{path:(?:community|notice|questions|knowledge)}/{bid}")
    public String getBoard(Model model, @PathVariable String path,
                           @RequestParam(value = "rvPage", defaultValue = "1") int page,
                           @PathVariable String bid, HttpSession session, HttpServletRequest request) {
        boardService.updateHits(Long.valueOf(bid), session);
        model = boardService.populateBoardModel(model, bid, request, page);

        // 에러 메시지가 있다면 모델에 추가
        if (model.containsAttribute("emptyContent")) {
            model.addAttribute("emptyContent", model.getAttribute("emptyContent"));
        }
        return "board/boardDetail";
    }

    @GetMapping("/{path:(?:community|questions|knowledge)}/new")
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

    @PostMapping("/{path:(?:community|questions|knowledge)}/new")
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

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @GetMapping("/notice/new")
    public String adminPost(Model model) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        BoardRequestDTO boardRequestDTO = new BoardRequestDTO();
        boardRequestDTO.setCategory("notice");
        boardRequestDTO.setEmail(email);
        model.addAttribute("boardRequestDTO", boardRequestDTO);
        model.addAttribute("category", "notice");
        return "board/noticePost";
    }
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PostMapping("/notice/new")
    public String adminPost(@Valid BoardRequestDTO boardRequestDTO, BindingResult bindingResult,
                       HttpServletResponse response, HttpServletRequest request) {
        if (bindingResult.hasErrors()) {
            bindingResult.getAllErrors().forEach(error -> {
                System.out.println(error.getDefaultMessage());
            });
        }
        log.info("BOARDREQUESTDTO : " + boardRequestDTO.getNotices());
        boardService.postBoard(response, request, boardRequestDTO);
        return "redirect:/notice";
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
