package com.community.communityproject.controller;

import com.community.communityproject.dto.TokenDTO;
import com.community.communityproject.dto.board.admin.BoardDTOForAdmin;
import com.community.communityproject.dto.comment.admin.CommentDTOForAdmin;
import com.community.communityproject.service.admin.AdminService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Slf4j
@Controller
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @GetMapping("/adminMenu")
    public String adminMenu(Model model) {
        return "admin/adminMenu";
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @GetMapping("/admin_manage_boards")
    public String manageBoards(Model model, HttpServletRequest request, HttpServletResponse response,
                               @RequestParam(value = "page", defaultValue = "1") int page,
                               @RequestParam(value = "kw", defaultValue = "") String kw,
                               @RequestParam(value = "sort", defaultValue = "LATEST") String sort,
                               @RequestParam(value = "category", defaultValue = "ALL") String category) {
        Page<BoardDTOForAdmin> boards = this.adminService.getBoardForAdmin(page, kw, sort, category, response, request);
        log.info("======================================");
        boards.stream().forEach(
                boardDTOForAdmin -> {
                    log.info("TITLE : " + boardDTOForAdmin.getTitle());
                }
        );
        log.info("======================================");
        model.addAttribute("boards", boards);
        model.addAttribute("category", category);
        model.addAttribute("kw", kw);
        return "admin/manageBoards";
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @GetMapping("/admin_manage_comments")
    public String manageComments(Model model, HttpServletRequest request, HttpServletResponse response,
                                 @RequestParam(value = "page", defaultValue = "1") int page,
                                 @RequestParam(value = "kw", defaultValue = "") String kw,
                                 @RequestParam(value = "sort", defaultValue = "LATEST") String sort,
                                 @RequestParam(value = "category", defaultValue = "ALL") String category,
                                 @RequestParam(value = "searchField", defaultValue = "") String searchField,
                                 @RequestParam(value = "option", defaultValue = "ALL") String option,
                                 @RequestParam(value = "option2", defaultValue = "ALL") String option2) {
        Page<CommentDTOForAdmin> comments = this.adminService.getCommentForAdmin(page, kw, sort, category, searchField, option, option2, response, request);
        log.info("======================================");
        comments.stream().forEach(
                commentDTOForAdmin ->
                        log.info("comment Id : " + commentDTOForAdmin.getCid())
        );
        log.info("======================================");
        model.addAttribute("comments", comments);
        model.addAttribute("category", category);
        model.addAttribute("kw", kw);
        return "admin/manageComments";
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PostMapping("/delete/comment/{cid}")
    public String deleteCommentForAdmin(@PathVariable String cid, Model model,
                                        HttpServletRequest request, HttpServletResponse response) {
        adminService.deleteComment(cid, request, response);
        // 요청 보낸 url 가져오기
        String refererUrl = request.getHeader("Referer");
        String baseUrl = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort();
        refererUrl = refererUrl.replaceFirst(baseUrl, "");
        return "redirect:" + refererUrl;
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @GetMapping("/admin_manage_users")
    public String manageUsers(Model model) {
        return "admin/manageUsers";
    }
}
