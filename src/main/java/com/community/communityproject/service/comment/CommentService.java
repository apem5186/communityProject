package com.community.communityproject.service.comment;

import com.community.communityproject.config.exception.CommentNotFoundException;
import com.community.communityproject.config.exception.CommentUserNotEqual;
import com.community.communityproject.config.exception.UserNotFoundException;
import com.community.communityproject.dto.TokenDTO;
import com.community.communityproject.dto.comment.CommentEditDTO;
import com.community.communityproject.dto.comment.CommentListResponseDTO;
import com.community.communityproject.dto.comment.CommentRequestDTO;
import com.community.communityproject.entity.board.Board;
import com.community.communityproject.entity.board.LikeStatus;
import com.community.communityproject.entity.comment.Comment;
import com.community.communityproject.entity.comment.CommentLike;
import com.community.communityproject.entity.users.Users;
import com.community.communityproject.repository.BoardRepository;
import com.community.communityproject.repository.CommentLikeRepository;
import com.community.communityproject.repository.CommentRepository;
import com.community.communityproject.repository.UserRepository;
import com.community.communityproject.service.jwt.AuthService;
import com.community.communityproject.service.jwt.TokenProvider;
import com.community.communityproject.service.users.UserService;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.actuate.endpoint.InvalidEndpointRequestException;
import org.springframework.data.domain.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    private final UserRepository userRepository;

    /**
     * 한 게시글의 댓글들 가져오기
     * @param page
     * @param bid
     * @return CommentDTO
     */
    @Transactional(readOnly = true)
    public Page<CommentListResponseDTO.CommentDTO> getCommentsFromBoard(int page, String bid, boolean loggedIn) {
        // 바로 Sort.by를 사용하여 정렬 조건을 설정
        Pageable pageable = PageRequest.of(page - 1, 10, Sort.by(Sort.Order.desc("regDate")));
        Board board = boardRepository.getReferenceById(Long.valueOf(bid));
        Page<Comment> comments = commentRepository.findAllByBoard(board, pageable);

        // loggedIn 변수에 따라 처리 방식을 다르게 하되, 하나의 스트림 연산으로 처리
        List<CommentListResponseDTO.CommentDTO> commentDTOs = comments.getContent().stream()
                .map(comment -> {
                    if (loggedIn) {
                        return new CommentListResponseDTO().getCommentDTOWithStatus(comment, this::checkLikeStatus);
                    } else {
                        return new CommentListResponseDTO().getCommentDTO(comment);
                    }
                })
                .collect(Collectors.toList());

        // commentDTO와 함께 PageImpl를 리턴
        return new PageImpl<>(commentDTOs, pageable, comments.getTotalElements());
    }

    /**
     * profile에 자기 댓글 가져오기
     * @param page
     * @param response
     * @param request
     * @return CommentDTO
     */
    public Page<CommentListResponseDTO.CommentDTO> getMyCommentListDTO(int page, HttpServletResponse response,
                                                                    HttpServletRequest request) {
        TokenDTO tokenDTO = authService.validateToken(response, request);
        if (tokenDTO != null) {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String email = authentication.getName();
            List<Sort.Order> sorts = new ArrayList<>();
            sorts.add(Sort.Order.desc("regDate"));
            Pageable pageable = PageRequest.of(page-1, 10, Sort.by(sorts));
            // Find all comments by user's Email
            Page<Comment> comments = commentRepository.findAllByUsersEmail(email, pageable);
            // Convert each Comment entity to CommentDTO
            List<CommentListResponseDTO.CommentDTO> commentDTOs = comments.getContent().stream()
                    .map(comment -> new CommentListResponseDTO().getCommentDTO(comment))
                    .toList();

            // Return a new PageImpl with the converted DTOs
            return new PageImpl<>(commentDTOs, pageable, comments.getTotalElements());
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
            Comment comment;
            if (commentRequestDTO.getParentId() == null) {
                comment = Comment.builder()
                        .content(commentRequestDTO.getContent())
                        .users(userService.getUsers())
                        .board(boardRepository.getReferenceById(Long.valueOf(bid)))
                        .build();
            } else {
                Comment parent = commentRepository.findById(commentRequestDTO.getParentId()).orElseThrow(CommentNotFoundException::new);
                comment = Comment.builder()
                        .content(commentRequestDTO.getContent())
                        .users(userService.getUsers())
                        .board(boardRepository.getReferenceById(Long.valueOf(bid)))
                        .parent(parent)
                        .build();
            }

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

    @Transactional
    public void editComment(HttpServletRequest request, HttpServletResponse response, CommentEditDTO commentEditDTO) {
        TokenDTO tokenDTO = authService.validateToken(response, request);
        if (tokenDTO != null) {
            Comment comment = commentRepository.findById(Long.valueOf(commentEditDTO.getCid())).orElseThrow(CommentNotFoundException::new);
            // 요청을 보낸 사용자와 댓글을 단 사용자가 같으면
            if (isOwnerOfComment(request, comment.getUsers().getEmail())) {
                comment.edit(commentEditDTO.getContent());
                log.info("바뀐 댓글 : " + comment.getContent());
                commentRepository.save(comment);
            } else throw new CommentUserNotEqual(); // 요청을 보낸 사람과 댓글 올린 사람이 다르면 예외 발생
        }
    }

    /**
     * 댓글 삭제
     * @param request
     * @param response
     * @param cid
     * @param bid
     */
    @Transactional
    public void deleteComment(HttpServletRequest request, HttpServletResponse response, String cid, String bid) {
        TokenDTO tokenDTO = authService.validateToken(response, request);
        if (tokenDTO != null) {
            Comment comment = commentRepository.findById(Long.valueOf(cid)).orElseThrow(CommentNotFoundException::new);
            // 요청을 보낸 사용자와 댓글을 단 사용자가 같으면
            if (isOwnerOfComment(request, comment.getUsers().getEmail())) {
                commentRepository.delete(comment);
                // 삭제 한 후 board의 리뷰 카운트 하락
                decreaseCnt(Long.valueOf(bid));
            } else throw new CommentUserNotEqual(); // 요청을 보낸 사람과 댓글 올린 사람이 다르면 예외 발생

        }else {
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

    /**
     * 댓글 추천, 비추천 로직
     * @param cid
     * @param response
     * @param request
     * @param status
     */
    @Transactional
    public void likeComment(Long cid, HttpServletResponse response, HttpServletRequest request, boolean status) {
        TokenDTO tokenDTO = authService.validateToken(response, request);
        if (tokenDTO != null) {
            Comment comment = commentRepository.getReferenceById(cid);
            // 일단 Board랑 Users 가져옴
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            Users users = userRepository.findByEmail(authentication.getName()).orElseThrow(UserNotFoundException::new);
            // 추천 버튼을 눌렀으면
            log.info("===============================");
            log.info("status : " + status);
            log.info("===============================");
            if (status) {
                // url 조작으로 추천 누른 상태인데 비추 요청 했을 때
                if (checkLikeStatus(cid) != null && checkLikeStatus(cid).equals("DISLIKE")) {
                    throw new InvalidEndpointRequestException("Invalid Endpoint", "추천 버튼을 누른 상태에서 비추 요청을 했습니다.");
                }
                // 추천 버튼을 누른적이 없을 때
                if (!hasLikeComment(comment, users)) {
                    comment.increaseLikeCnt();
                    CommentLike commentLike = new CommentLike(comment, users, LikeStatus.LIKE);
                    commentLikeRepository.save(commentLike);
                } else {    // 있을 때
                    comment.decreaseLikeCnt();
                    CommentLike commentLike = commentLikeRepository.findByCommentAndUsers(comment, users).get();
                    commentLikeRepository.delete(commentLike);
                }
            } else {    // 비추천 버튼을 눌렀으면
                // url 조작으로 비추 누른 상탠데 추천 요청 했을 때
                if (checkLikeStatus(cid) != null && checkLikeStatus(cid).equals("LIKE")) {
                    throw new InvalidEndpointRequestException("Invalid Endpoint", "비추 버튼을 누른 상태에서 추천 요청을 했습니다.");
                }
                // 비추천 버튼을 누른적이 없을 때
                if (!hasLikeComment(comment, users)) {
                    comment.decreaseLikeCnt();
                    CommentLike commentLike = new CommentLike(comment, users, LikeStatus.DISLIKE);
                    commentLikeRepository.save(commentLike);
                } else {    // 있을 때
                    comment.increaseLikeCnt();
                    CommentLike commentLike = commentLikeRepository.findByCommentAndUsers(comment, users).get();
                    commentLikeRepository.delete(commentLike);
                }
            }
        }
    }

    /**
     * LIKE인지 DISLIKE인지 판별
     * @return null 아니면 enum 타입의 LIKE or DISLIKE
     */
    public String checkLikeStatus(Long cid) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        Users users = userRepository.findByEmail(email).orElseThrow(UserNotFoundException::new);
        Comment comment = commentRepository.findById(cid).orElseThrow(CommentNotFoundException::new);
        // 추천이나 비추를 누른 기록이 있다면
        if (hasLikeComment(comment, users)) {
            return commentLikeRepository.findByCommentAndUsers(comment, users).get().getLikeStatus().toString();
        }
        // 없으면 null
        return null;
    }

    /**
     * 일단 유저가 현재 있는 댓글 추천이든 비추든 눌렀는지 확인함 뭐든 일단 눌렀으면
     * true 아니면 false
     * @param comment
     * @param users
     * @return true or false
     */
    public boolean hasLikeComment(Comment comment, Users users) {
        return commentLikeRepository.findByCommentAndUsers(comment, users).isPresent();
    }

    /**
     * 댓글 쓴 사람과 어떤 요청을 보낸 사람이 동일한지 비교함
     * @param request
     * @param commentOwnerEmail
     * @return true or false
     */
    public boolean isOwnerOfComment(HttpServletRequest request, String commentOwnerEmail) {
        String requestEmail = request.getRemoteUser();
        log.info("REQUEST EMAIL : " + requestEmail);
        return requestEmail.equals(commentOwnerEmail);
    }
}
