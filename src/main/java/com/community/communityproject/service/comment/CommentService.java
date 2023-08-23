package com.community.communityproject.service.comment;

import com.community.communityproject.dto.TokenDTO;
import com.community.communityproject.dto.comment.CommentListResponseDTO;
import com.community.communityproject.dto.comment.CommentRequestDTO;
import com.community.communityproject.entity.board.Board;
import com.community.communityproject.entity.comment.Comment;
import com.community.communityproject.entity.users.Users;
import com.community.communityproject.repository.BoardRepository;
import com.community.communityproject.repository.CommentLikeRepository;
import com.community.communityproject.repository.CommentRepository;
import com.community.communityproject.service.jwt.AuthService;
import com.community.communityproject.service.jwt.TokenProvider;
import com.community.communityproject.service.users.UserService;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CommentService {

    private final BoardRepository boardRepository;
    private final CommentRepository commentRepository;
    private final CommentLikeRepository commentLikeRepository;
    private final AuthService authService;
    private final UserService userService;
    private final TokenProvider tokenProvider;

    /**
     * 한 게시글의 댓글들 가져오기
     * @param page
     * @param bid
     * @return CommentDTO
     */
    @Transactional
    public Page<CommentListResponseDTO.CommentDTO> getCommentsFromBoard(int page, String bid) {
        List<Sort.Order> sorts = new ArrayList<>();
        sorts.add(Sort.Order.desc("regDate"));
        Pageable pageable = PageRequest.of(page-1, 10, Sort.by(sorts));
        Board board = boardRepository.getReferenceById(Long.valueOf(bid));
        Page<Comment> comments = commentRepository.findAllByBoard(board, pageable);

        // CommentDTO로 각 Comment들을 변환
        List<CommentListResponseDTO.CommentDTO> commentDTOs = comments.getContent().stream()
                .map(comment -> new CommentListResponseDTO().getCommentDTO(comment))
                .collect(Collectors.toList());

        // commentDTO와 함께 PageImpl를 리턴
        return new PageImpl<>(commentDTOs, pageable, comments.getTotalElements());
    }

    /**
     * 댓글 작성
     * @param request
     * @param response
     * @param commentRequestDTO
     * @param bid
     */
    @Transactional
    public void PostComment(HttpServletRequest request, HttpServletResponse response,
                            CommentRequestDTO commentRequestDTO, String bid) {

        TokenDTO tokenDTO = authService.validateToken(response, request);
        if (tokenDTO != null) {

            Comment comment = Comment.builder()
                    .content(commentRequestDTO.getContent())
                    .users(userService.getUsers())
                    .board(boardRepository.getReferenceById(Long.valueOf(bid)))
                    .build();

            commentRepository.save(comment);
            increaseCnt(Long.valueOf(bid));
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

    /**
     * 댓글 등록하고 게시글의 reviewCnt 필드의 수 증가
     * @param bid
     */
    private void increaseCnt(Long bid) {
        Board board = boardRepository.getReferenceById(bid);
        board.increaseReviewCnt();
        boardRepository.save(board);
    }

    /**
     * 댓글 삭제하고 게시글의 reviewCnt 필드의 수 하락
     * @param bid
     */
    private void decreaseCnt(Long bid) {
        Board board = boardRepository.getReferenceById(bid);
        board.decreaseReviewCnt();
        boardRepository.save(board);
    }
}
