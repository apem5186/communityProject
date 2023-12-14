package com.community.communityproject.service.admin;

import com.community.communityproject.dto.TokenDTO;
import com.community.communityproject.dto.board.admin.BoardDTOForAdmin;
import com.community.communityproject.dto.comment.admin.CommentDTOForAdmin;
import com.community.communityproject.dto.users.admin.UsersDTOForAdmin;
import com.community.communityproject.entity.board.Board;
import com.community.communityproject.entity.comment.Comment;
import com.community.communityproject.entity.users.Users;
import com.community.communityproject.repository.BoardRepository;
import com.community.communityproject.repository.CommentRepository;
import com.community.communityproject.repository.UserRepository;
import com.community.communityproject.service.jwt.AuthService;
import com.community.communityproject.service.jwt.TokenProvider;
import com.community.communityproject.service.util.BoardUtilService;
import com.community.communityproject.service.util.CommentUtilService;
import com.community.communityproject.service.util.UsersUtilService;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminService {

    private final UserRepository userRepository;
    private final BoardRepository boardRepository;
    private final CommentRepository commentRepository;
    private final AuthService authService;
    private final BoardUtilService boardUtilService;
    private final TokenProvider tokenProvider;
    private final CommentUtilService commentUtilService;
    private final UsersUtilService usersUtilService;

    /**
     * 전체 게시글 가져옴 키워드, 카테고리, 드롭다운에 있는 정렬 값들로 옵션 설정 가능
     * @param page
     * @param kw
     * @param sort
     * @param category
     * @param response
     * @param request
     * @return boardDTOForAdmin
     */
    public Page<BoardDTOForAdmin> getBoardForAdmin(int page, String kw, String sort, String category,
            HttpServletResponse response, HttpServletRequest request) {
        TokenDTO tokenDTO = authService.validateToken(response, request);
        if (tokenDTO != null) {
            List<Sort.Order> sorts = new ArrayList<>();
            // 정렬 기준 LATEST, VOTE_COUNT, COMMENT_COUNT, HITS_COUNT
            // default = LATEST
            sort = boardUtilService.sort(sort);
            sorts.add(Sort.Order.desc(sort));
            Pageable pageable = PageRequest.of(page-1, 10, Sort.by(sorts));
            Specification<Board> spec = boardUtilService.search(kw);
            if (!Objects.equals(category, "ALL")) {
                spec = spec.and(boardUtilService.category(category));
            }
            Page<Board> boards =  boardRepository.findAll(spec, pageable);

            List<BoardDTOForAdmin> boardDTOForAdmins = boards.stream().map(
                    board -> BoardDTOForAdmin.builder()
                            .board(board)
                            .build()
            ).toList();
            return new PageImpl<>(boardDTOForAdmins, pageable, boards.getTotalElements());
        } else {
            String at = null;
            Cookie[] cookies = request.getCookies();
            for (Cookie cookie :
                    cookies) {
                if (cookie.getName().equals("access-token"))
                    at = cookie.getValue();
            }
            throw new ExpiredJwtException(tokenProvider.getHeader(at), tokenProvider.getClaims(at), "Expired Token");
        }
    }

    public Page<CommentDTOForAdmin> getCommentForAdmin(int page, String kw, String sort, String category,
                                                       String searchField, String option, String option2,
                                                       HttpServletResponse response, HttpServletRequest request) {
        TokenDTO tokenDTO = authService.validateToken(response, request);
        if (tokenDTO != null) {
            List<Sort.Order> sorts = new ArrayList<>();
            // 정렬 기준 LATEST, OLDEST, VOTE_COUNT, REPLY_COUNT
            // default = LATEST
            if (sort.equals("OLDEST")) {
                sort = commentUtilService.sort(sort);
                sorts.add(Sort.Order.asc(sort));
            } else {
                sort = commentUtilService.sort(sort);
                sorts.add(Sort.Order.desc(sort));
            }
            Pageable pageable = PageRequest.of(page-1, 10, Sort.by(sorts));
            Specification<Comment> spec = commentUtilService.search(kw, searchField);;
            if (!Objects.equals(category, "ALL")) {
                spec = spec.and(commentUtilService.category(category));
            }
            if (!Objects.equals(option, "ALL")) {
                spec = spec.and(commentUtilService.filterByUserRole(option));
            }
            if (!Objects.equals(option2, "ALL")) {
                spec = spec.and(commentUtilService.filterByOption2(option2));
            }
            Page<Comment> comments = commentRepository.findAll(spec, pageable);
            List<CommentDTOForAdmin> commentDTOForAdmins = comments.stream().map(
                    comment -> CommentDTOForAdmin.builder()
                            .comment(comment)
                            .build()
            ).toList();
            return new PageImpl<>(commentDTOForAdmins, pageable, comments.getTotalElements());
        } else {
            String at = null;
            Cookie[] cookies = request.getCookies();
            for (Cookie cookie :
                    cookies) {
                if (cookie.getName().equals("access-token"))
                    at = cookie.getValue();
            }
            throw new ExpiredJwtException(tokenProvider.getHeader(at), tokenProvider.getClaims(at), "Expired Token");
        }
    }

    @Transactional
    public void deleteComment(String cid, HttpServletRequest request, HttpServletResponse response) {
        TokenDTO tokenDTO = authService.validateToken(response, request);
        if (tokenDTO != null) {
            commentRepository.deleteById(Long.valueOf(cid));
        } else {
            String at = null;
            Cookie[] cookies = request.getCookies();
            for (Cookie cookie :
                    cookies) {
                if (cookie.getName().equals("access-token"))
                    at = cookie.getValue();
            }
            throw new ExpiredJwtException(tokenProvider.getHeader(at), tokenProvider.getClaims(at), "Expired Token");
        }
    }

    @Transactional
    public void deleteUser(String uid, HttpServletResponse response, HttpServletRequest request) {
        TokenDTO tokenDTO = authService.validateToken(response, request);
        if (tokenDTO != null) {
            userRepository.deleteById(Long.valueOf(uid));
        } else {
            String at = null;
            Cookie[] cookies = request.getCookies();
            for (Cookie cookie :
                    cookies) {
                if (cookie.getName().equals("access-token"))
                    at = cookie.getValue();
            }
            throw new ExpiredJwtException(tokenProvider.getHeader(at), tokenProvider.getClaims(at), "Expired Token");
        }
    }

    public Page<UsersDTOForAdmin> getUsersForAdmin(int page, String kw, String sort, String searchField, String option, String option2,
                                                   HttpServletResponse response, HttpServletRequest request) {
        TokenDTO tokenDTO = authService.validateToken(response, request);
        if (tokenDTO != null) {
            List<Sort.Order> sorts = new ArrayList<>();
            if (sort.equals("LATEST"))
                sorts.add(Sort.Order.desc("regDate"));
            else if (sort.equals("OLDEST"))
                sorts.add(Sort.Order.asc("regDate"));
            Pageable pageable = PageRequest.of(page-1, 10, Sort.by(sorts));
            Specification<Users> spec = usersUtilService.search(kw, searchField);
            if (!Objects.equals(option, "ALL"))
                spec = spec.and(usersUtilService.filterByUserRole(option));
            if (!Objects.equals(option2, "ALL"))
                spec = spec.and(usersUtilService.filterByOption2(option2));
            Page<Users> users = userRepository.findAll(spec, pageable);
            List<UsersDTOForAdmin> usersDTOForAdmins = users.stream().map(
                    user -> UsersDTOForAdmin.builder()
                            .users(user)
                            .build()
            ).toList();
            return new PageImpl<>(usersDTOForAdmins, pageable, users.getTotalElements());
        } else {
            String at = null;
            Cookie[] cookies = request.getCookies();
            for (Cookie cookie :
                    cookies) {
                if (cookie.getName().equals("access-token"))
                    at = cookie.getValue();
            }
            throw new ExpiredJwtException(tokenProvider.getHeader(at), tokenProvider.getClaims(at), "Expired Token");
        }
    }
}
