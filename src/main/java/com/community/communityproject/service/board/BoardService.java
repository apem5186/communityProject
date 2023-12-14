package com.community.communityproject.service.board;

import com.amazonaws.services.s3.AmazonS3Client;
import com.community.communityproject.config.AmazonS3ResourceStorage;
import com.community.communityproject.config.exception.BoardNotFoundException;
import com.community.communityproject.config.exception.FavoriteNotFoundException;
import com.community.communityproject.config.exception.UserNotFoundException;
import com.community.communityproject.dto.TokenDTO;
import com.community.communityproject.dto.board.*;
import com.community.communityproject.dto.comment.CommentLikeDTO;
import com.community.communityproject.dto.comment.CommentListResponseDTO;
import com.community.communityproject.entity.board.*;
import com.community.communityproject.entity.users.Users;
import com.community.communityproject.repository.*;
import com.community.communityproject.service.comment.CommentService;
import com.community.communityproject.service.jwt.AuthService;
import com.community.communityproject.service.jwt.TokenProvider;
import com.community.communityproject.service.users.UserService;
import com.community.communityproject.service.util.BoardUtilService;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.persistence.criteria.*;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.endpoint.InvalidEndpointRequestException;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;

import java.io.Serial;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class BoardService {

    private final BoardRepository boardRepository;
    private final BoardImageRepository boardImageRepository;
    private final UserRepository userRepository;
    private final UserService userService;
    private final AuthService authService;
    private final TokenProvider tokenProvider;
    private final AmazonS3ResourceStorage amazonS3ResourceStorage;
    private final AmazonS3Client amazonS3Client;
    private final BoardLikeRepository boardLikeRepository;
    private final BoardFavoriteRepository boardFavoriteRepository;
    private final CommentService commentService;
    private final BoardUtilService boardUtilService;

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    private final String BOARD_PATH = "boardImage/";

    /**
     * BoardController getBoard 메소드에 Model 추가시키는 서비스
     * @param model
     * @param bid
     * @param request
     * @param page
     * @return model
     */
    public Model populateBoardModel(Model model, String bid, HttpServletRequest request, int page) {
        Long boardId = Long.valueOf(bid);
        BoardListResponseDTO.BoardDTO boardDTO = getBoard(Long.valueOf(bid));
        model.addAttribute("userEmail", request.getRemoteUser());
        String likeStatus = boardUtilService.checklikeStatus(boardId);
        model.addAttribute("board", boardDTO);
        log.info("REQUEST user : " + request.getRemoteUser() + " AND " + request.getAuthType());
        // 권한이 있는 사용자만
        if (request.isUserInRole("ROLE_USER") || request.isUserInRole("ROLE_ADMIN")) {
            log.info("REQUEST USER : " + request.getRemoteUser() + " and " + request.getAuthType());
            // comment는 DTO 안에 추천 status 필드를 만들어서 함
            // comment 로직 분리 예정
            Page<CommentListResponseDTO.CommentDTO> commentDTO = commentService.getCommentsFromBoard(page, bid, true);
            model.addAttribute("comments", commentDTO);
            model.addAttribute("commentLikeDTO", new CommentLikeDTO());
            // board는 추천 status와 즐찾 status를 따로 모델에 추가했음 나중에 한가지 방법으로 통일하는 것도 고려해야함
            boolean isFavorite = boardUtilService.hasFavoriteBoard(boardId);
            if (likeStatus != null) {
                model.addAttribute("likeStatus", likeStatus);
            }
            if (isFavorite) {
                model.addAttribute("isFavorite", isFavorite);
            }
        } else {
            log.info("REQUEST user in not loggedIn : " + request.getRemoteUser() + " AND " + request.getAuthType());
            // 로그인 안했으면 CommentDTO의 likeStatus에 값을 안넣음
            // comment 로직 분리 예정
            Page<CommentListResponseDTO.CommentDTO> commentDTO = commentService.getCommentsFromBoard(page, bid, false);
            model.addAttribute("comments", commentDTO);
        }
        return model;
    }

    /**
     * 한 게시글의 데이터를 가져오기 위한 service
     * @param bid
     * @return BoardListResponseDTO.BoardDTO
     */
    @Transactional(readOnly = true)
    public BoardListResponseDTO.BoardDTO getBoard(Long bid) {
        Board board = boardRepository.getReferenceById(bid);
        if (board.getContent().isEmpty()) {
            throw new BoardNotFoundException();
        }
        return new BoardListResponseDTO().new BoardDTO(board);
    }

    /**
     * 다른 사람 프로필에서 게시글 불러올 때
     * @param page
     * @param uid
     * @return boardDTO
     */
    public Page<BoardListResponseDTO.BoardDTO> getOtherUserBoardListDTO(int page, String uid) {
        List<Sort.Order> sorts = new ArrayList<>();
        sorts.add(Sort.Order.desc(boardUtilService.sort("LATEST")));
        Pageable pageable = PageRequest.of(page-1, 10, Sort.by(sorts));
        Page<Board> boards = boardRepository.findAllByUsersId(Long.valueOf(uid), pageable);

        // Convert each Board entity to BoardDTO
        List<BoardListResponseDTO.BoardDTO> boardDTOs = boards.getContent().stream()
                .map(board -> new BoardListResponseDTO().getBoardDTO(board))
                .collect(Collectors.toList());

        // Return a new PageImpl with the converted DTOs
        return new PageImpl<>(boardDTOs, pageable, boards.getTotalElements());
    }
    /**
     * 프로필에서 내 게시글 불러올 때 씀 정렬 기준은 없으므로 Specification 안 씀 기준은 최신순으로
     * @param page
     * @return boardDTO
     */
    public Page<BoardListResponseDTO.BoardDTO> getMyBoardListDTO(int page, HttpServletResponse response,
                                                                 HttpServletRequest request) {
        TokenDTO tokenDTO = authService.validateToken(response, request);
        if (tokenDTO != null) {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String email = authentication.getName();
            List<Sort.Order> sorts = new ArrayList<>();
            sorts.add(Sort.Order.desc(boardUtilService.sort("LATEST")));
            Pageable pageable = PageRequest.of(page-1, 10, Sort.by(sorts));
            Page<Board> boards = boardRepository.findAllByUsersEmail(email, pageable);

            // Convert each Board entity to BoardDTO
            List<BoardListResponseDTO.BoardDTO> boardDTOs = boards.getContent().stream()
                    .map(board -> new BoardListResponseDTO().getBoardDTO(board))
                    .collect(Collectors.toList());

            // Return a new PageImpl with the converted DTOs
            return new PageImpl<>(boardDTOs, pageable, boards.getTotalElements());
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
     * 남의 프로필에서 좋아요 누른 게시글 찾는 거
     * @param page
     * @param uid
     * @return boardLikeDTO
     */
    public Page<BoardLikeDTO> getOtherUserLikeListDTO(int page, String uid) {
        List<Sort.Order> sorts = new ArrayList<>();
        sorts.add(Sort.Order.desc(boardUtilService.sort("LATEST")));
        Pageable pageable = PageRequest.of(page-1, 10, Sort.by(sorts));
        Page<Board> boards = boardLikeRepository.findBoardLikesByUsersId(Long.valueOf(uid), pageable);

        // Convert each Board entity to BoardLikeDTO
        List<BoardLikeDTO> boardLikeDTOs = boards.getContent().stream()
                .map(board -> {
                    BoardListResponseDTO.BoardDTO boardDTO = new BoardListResponseDTO().getBoardDTO(board);
                    String status = boardUtilService.checklikeStatus(board.getId()); // This is a hypothetical method; you'll need to provide its implementation.
                    return new BoardLikeDTO(boardDTO, status);
                })
                .collect(Collectors.toList());

        // Return a new PageImpl with the converted DTOs
        return new PageImpl<>(boardLikeDTOs, pageable, boards.getTotalElements());
    }

    /**
     * 좋아요 한 게시글 리스트 불러오기
     * @param page
     * @param response
     * @param request
     * @return boardLikeDTO
     */
    public Page<BoardLikeDTO> getMyLikeListDTO(int page, HttpServletResponse response,
                                                                HttpServletRequest request) {
        TokenDTO tokenDTO = authService.validateToken(response, request);
        if (tokenDTO != null) {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String email = authentication.getName();
            Users users = userRepository.findByEmail(email).orElseThrow(UserNotFoundException::new);
            List<Sort.Order> sorts = new ArrayList<>();
            sorts.add(Sort.Order.desc(boardUtilService.sort("LATEST")));
            Pageable pageable = PageRequest.of(page-1, 10, Sort.by(sorts));
            Page<Board> boards = boardLikeRepository.findBoardLikesByUsers(users, pageable);

            // Convert each Board entity to BoardLikeDTO
            List<BoardLikeDTO> boardLikeDTOs = boards.getContent().stream()
                    .map(board -> {
                        BoardListResponseDTO.BoardDTO boardDTO = new BoardListResponseDTO().getBoardDTO(board);
                        String status = boardUtilService.checklikeStatus(board.getId()); // This is a hypothetical method; you'll need to provide its implementation.
                        return new BoardLikeDTO(boardDTO, status);
                    })
                    .collect(Collectors.toList());

            // Return a new PageImpl with the converted DTOs
            return new PageImpl<>(boardLikeDTOs, pageable, boards.getTotalElements());
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
     * 남의 게시글에서 즐겨찾기한 게시글 찾는 용도
     * @param page
     * @param uid
     * @return boardDTO
     */
    public Page<BoardListResponseDTO.BoardDTO> getOtherUserFavoriteListDTO(int page, String uid) {
        List<Sort.Order> sorts = new ArrayList<>();
        sorts.add(Sort.Order.desc(boardUtilService.sort("LATEST")));
        Pageable pageable = PageRequest.of(page-1, 10, Sort.by(sorts));
        Page<Board> boards = boardFavoriteRepository.findBoardFavoritesByUsersId(Long.valueOf(uid), pageable);

        // Convert each Board entity to BoardDTO
        List<BoardListResponseDTO.BoardDTO> boardDTOs = boards.getContent().stream()
                .map(board -> new BoardListResponseDTO().getBoardDTO(board))
                .collect(Collectors.toList());

        // Return a new PageImpl with the converted DTOs
        return new PageImpl<>(boardDTOs, pageable, boards.getTotalElements());
    }

    /**
     * 즐겨찾기한 board 가져옴
     * @param page
     * @return boardDTO
     */
    public Page<BoardListResponseDTO.BoardDTO> getMyFavoriteListDTO(int page, HttpServletResponse response,
                                                                 HttpServletRequest request) {
        TokenDTO tokenDTO = authService.validateToken(response, request);
        if (tokenDTO != null) {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String email = authentication.getName();
            Users users = userRepository.findByEmail(email).orElseThrow(UserNotFoundException::new);
            List<Sort.Order> sorts = new ArrayList<>();
            sorts.add(Sort.Order.desc(boardUtilService.sort("LATEST")));
            Pageable pageable = PageRequest.of(page-1, 10, Sort.by(sorts));
            Page<Board> boards = boardFavoriteRepository.findBoardFavoritesByUsers(users, pageable);

            // Convert each Board entity to BoardDTO
            List<BoardListResponseDTO.BoardDTO> boardDTOs = boards.getContent().stream()
                    .map(board -> new BoardListResponseDTO().getBoardDTO(board))
                    .collect(Collectors.toList());

            // Return a new PageImpl with the converted DTOs
            return new PageImpl<>(boardDTOs, pageable, boards.getTotalElements());
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
     * keyword, category를 이용해 게시글을 리턴함
     * BoardListResponseDTO를 사용
     * @param page
     * @param kw
     * @param category
     * @return boardDTO
     */
    public Page<BoardListResponseDTO.BoardDTO> getBoardListDTO(int page, String kw, String sort, String category) {
        List<Sort.Order> sorts = new ArrayList<>();
        // 정렬 기준 LATEST, VOTE_COUNT, COMMENT_COUNT, HITS_COUNT
        // default = LATEST
        sort = boardUtilService.sort(sort);
        sorts.add(Sort.Order.desc(sort));
        Pageable pageable = PageRequest.of(page-1, 10, Sort.by(sorts));
        Specification<Board> spec = boardUtilService.search(kw);
        spec = spec.and(boardUtilService.category(category));
        Page<Board> boards =  boardRepository.findAll(spec, pageable);

        // Convert each Board entity to BoardDTO
        List<BoardListResponseDTO.BoardDTO> boardDTOs = boards.getContent().stream()
                .map(board -> new BoardListResponseDTO().getBoardDTO(board))
                .collect(Collectors.toList());

        // Return a new PageImpl with the converted DTOs
        return new PageImpl<>(boardDTOs, pageable, boards.getTotalElements());
    }

    public List<BoardListResponseDTO.BoardDTO> getBoardListDTO5(String category) {
        List<Sort.Order> sorts = new ArrayList<>();
        sorts.add(Sort.Order.desc("regDate"));
        Pageable pageable = PageRequest.of(0, 5, Sort.by(sorts));
        Specification<Board> spec = boardUtilService.category(category);
        Page<Board> boards =  boardRepository.findAll(spec, pageable);

        // Convert each Board entity to BoardDTO
        return boards.getContent().stream()
                .map(board -> new BoardListResponseDTO().getBoardDTO(board))
                .collect(Collectors.toList());
    }

    /**
     * 공지 게시글 반환
     * @param page
     * @param category
     * @return boardDTOs
     */
    public Page<BoardListResponseDTO.BoardDTO> getNoticeBoardListDTO(int page, String category) {
        // 입력 카테고리 문자열을 기반으로 카테고리 세트 생성
        Set<Category> categories = new HashSet<>();
        categories.add(Category.valueOf(category.toUpperCase()));

        // 'regDate' 기준으로 정렬 최신으로 6개만 가져옴 나머진 공지 게시판 가서 봐야함
        Pageable pageable = PageRequest.of(page-1, 6, Sort.by(Sort.Order.desc("regDate")));

        // 공지 Set에 주어진 카테고리가 있는 보드를 찾음
        Page<Board> boards = boardRepository.findByNoticesIn(categories, pageable);

        // 각 Board 엔터티를 BoardDTO로 변환
        List<BoardListResponseDTO.BoardDTO> boardDTOs = boards.getContent().stream()
                .map(board -> new BoardListResponseDTO().getBoardDTO(board))
                .collect(Collectors.toList());

        // 변환된 DTO를 가진 새 PageImpl 반환
        return new PageImpl<>(boardDTOs, pageable, boards.getTotalElements());
    }


    /**
     * 게시글 등록 부분
     * 토큰 검사 진행 후 이미지 없으면 게시글만 등록
     * 있으면 게시글 등록 후 이미지 등록
     * @param response
     * @param request
     * @param boardRequestDTO
     */
    @Transactional
    public void postBoard(HttpServletResponse response, HttpServletRequest request,
                          BoardRequestDTO boardRequestDTO) {
        log.info("=============================================");
        log.info("=============================================");
        log.info("=============================================");
        log.info("BoardRequestDTO : " + boardRequestDTO.getTitle());
        log.info("content : " + boardRequestDTO.getContent());
        log.info("category : " + boardRequestDTO.getCategory());
        Set<Category> noticeCategories = boardRequestDTO.getNotices();
        log.info("Notice Categories: " + noticeCategories);
        log.info("=============================================");
        log.info("=============================================");
        log.info("=============================================");
        TokenDTO tokenDTO = authService.validateToken(response, request);
        if (tokenDTO != null) {
            Users users = userRepository.findByEmail(boardRequestDTO.getEmail()).orElseThrow();
            if (Objects.equals(boardRequestDTO.getCategory(), "notice")) {
                Board board = Board.NoticeBuilder()
                        .title(boardRequestDTO.getTitle())
                        .content(boardRequestDTO.getContent())
                        .category(Category.NOTICE)
                        .users(users)
                        .build();
                board.getNotices().addAll(boardRequestDTO.getNotices());
                log.info("BOARD NOTICES : " + board.getNotices());
                Board savedBoard = boardRepository.save(board);
                boardUtilService.postS3(boardRequestDTO, board);
                log.info("BOARD : " + savedBoard.getNotices());
            } else {
                Board board = Board.builder()
                        .title(boardRequestDTO.getTitle())
                        .content(boardRequestDTO.getContent())
                        .category(Category.valueOf(boardRequestDTO.getCategory().toUpperCase()))
                        .users(users)
                        .build();
                // db에 이미지 넣을라면 게시글 정보 먼저 넣어야 함
                boardRepository.save(board);
                boardUtilService.postS3(boardRequestDTO, board);
            }
        }
    }

    public BoardEditRequestDTO setEditBoard(Long bid) {
        Board board = boardRepository.getReferenceById(bid);
        BoardEditRequestDTO boardEditRequestDTO = new BoardEditRequestDTO();
        boardEditRequestDTO.setBid(bid);
        boardEditRequestDTO.setTitle(board.getTitle());
        boardEditRequestDTO.setCategory(board.getCategory().toString());
        boardEditRequestDTO.setContent(board.getContent());
        return boardEditRequestDTO;
    }

    /**
     * board를 수정함 이미지가 있을때와 없을때로 구분
     * @param request
     * @param response
     * @param boardEditRequestDTO
     */
    @Transactional
    public void editBoard(HttpServletRequest request, HttpServletResponse response, BoardEditRequestDTO boardEditRequestDTO){
        TokenDTO tokenDTO = authService.validateToken(response, request);
        if (tokenDTO != null) {
            // 저장할 이미지가 있든 없든 일단 지움
            ArrayList<BoardImage> boardImages = boardImageRepository.findBoardImageByBoardId(boardEditRequestDTO.getBid());
            for (BoardImage boardImage :
                    boardImages) {
                String path = boardUtilService.trimUrlToPath(boardImage.getFilePath());
                if (amazonS3Client.doesObjectExist(bucket, path)) {
                    amazonS3Client.deleteObject(bucket, path);
                }
                boardImageRepository.delete(boardImage);
            }
            // 저장할 이미지가 없다면 board만 수정
            Board board = boardRepository.getReferenceById(boardEditRequestDTO.getBid());
            // 수정할 게시글이 공지 게시글이면 Board의 noticeEdit
            if (boardEditRequestDTO.getCategory().equals("notice")) {
                board.noticeEdit(boardEditRequestDTO.getTitle(), boardEditRequestDTO.getContent(),
                        boardEditRequestDTO.getNotices());
            } else {    // 아니면 그냥 Board의 edit
                board.edit(boardEditRequestDTO.getTitle(), boardEditRequestDTO.getContent(),
                        boardEditRequestDTO.getCategory().toUpperCase());
            }
            boardRepository.save(board);
            // 저장할 이미지가 있다면 s3에 올림
            if (!boardEditRequestDTO.getBoardImage().isEmpty()) {
                boardUtilService.postS3(boardEditRequestDTO, board);
            }
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
     * Board 삭제
     * @param request
     * @param response
     * @param bid
     */
    @Transactional
    public void deleteBoard(HttpServletRequest request, HttpServletResponse response, Long bid) {
        TokenDTO tokenDTO = authService.validateToken(response, request);
        if (tokenDTO != null) {
            Board board = boardRepository.getReferenceById(bid);
            // 일단 s3에 있는 이미지 지움
            ArrayList<BoardImage> boardImages = boardImageRepository.findBoardImageByBoardId(bid);
            for (BoardImage boardImage :
                    boardImages) {
                String path = boardUtilService.trimUrlToPath(boardImage.getFilePath());
                if (amazonS3Client.doesObjectExist(bucket, path))
                    amazonS3Client.deleteObject(bucket, path);
            }
            boardRepository.delete(board);
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
     * 게시글 방문할 때 조회수를 늘림 늘리는 방식은 HTTP Session을 이용
     * @param bid
     * @param session
     */
    @Transactional
    public void updateHits(Long bid, HttpSession session) {
        Set<Long> viewedBoards = (Set<Long>) session.getAttribute("viewedBoards");
        if (viewedBoards == null) {
            viewedBoards = new HashSet<>();
        }
        if (!viewedBoards.contains(bid)) {
            this.boardRepository.updateHits(Math.toIntExact(bid));
            viewedBoards.add(bid);
            session.setAttribute("viewedBoards", viewedBoards);
        }
    }

    /**
     * 게시글 추천, 비추천 로직
     * @param bid
     * @param response
     * @param request
     * @param status
     */
    @Transactional
    public void likeBoard(Long bid, HttpServletResponse response, HttpServletRequest request, boolean status) {
        TokenDTO tokenDTO = authService.validateToken(response, request);
        if (tokenDTO != null) {
            // 일단 Board랑 Users 가져옴
            Board board = boardRepository.getReferenceById(bid);
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            Users users = userRepository.findByEmail(authentication.getName()).orElseThrow(UserNotFoundException::new);
            // 추천 버튼을 눌렀으면
            log.info("===============================");
            log.info("status : " + status);
            log.info("===============================");
            if (status) {
                // url 조작으로 추천 누른 상태인데 비추 요청 했을 때
                if (boardUtilService.checklikeStatus(bid) != null && boardUtilService.checklikeStatus(bid).equals("DISLIKE")) {
                    throw new InvalidEndpointRequestException("Invalid Endpoint", "추천 버튼을 누른 상태에서 비추 요청을 했습니다.");
                }
                // 추천 버튼을 누른적이 없을 때
                if (!boardUtilService.hasLikeBoard(board, users)) {
                    board.increaseLikeCnt();
                    BoardLike boardLike = new BoardLike(board, users, LikeStatus.LIKE);
                    boardLikeRepository.save(boardLike);
                } else {    // 있을 때
                    board.decreaseLikeCnt();
                    BoardLike boardLike = boardLikeRepository.findByBoardAndUsers(board, users).get();
                    boardLikeRepository.delete(boardLike);
                }
            } else {    // 비추천 버튼을 눌렀으면
                // url 조작으로 비추 누른 상탠데 추천 요청 했을 때
                if (boardUtilService.checklikeStatus(bid) != null && boardUtilService.checklikeStatus(bid).equals("LIKE")) {
                    throw new InvalidEndpointRequestException("Invalid Endpoint", "비추 버튼을 누른 상태에서 추천 요청을 했습니다.");
                }
                // 비추천 버튼을 누른적이 없을 때
                if (!boardUtilService.hasLikeBoard(board, users)) {
                    board.decreaseLikeCnt();
                    BoardLike boardLike = new BoardLike(board, users, LikeStatus.DISLIKE);
                    boardLikeRepository.save(boardLike);
                } else {    // 있을 때
                    board.increaseLikeCnt();
                    BoardLike boardLike = boardLikeRepository.findByBoardAndUsers(board, users).get();
                    boardLikeRepository.delete(boardLike);
                }
            }
        }
    }

    /**
     * 즐찾 기능, 누른 적 없으면 테이블 생성, 있으면 테이블 제거
     * @param id
     * @param response
     * @param request
     */
    @Transactional
    public void favoriteBoard(Long id, HttpServletResponse response, HttpServletRequest request) {
        TokenDTO tokenDTO = authService.validateToken(response, request);
        if (tokenDTO != null) {
            // 일단 Board랑 Users 가져옴
            Board board = boardRepository.getReferenceById(id);
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            Users users = userRepository.findByEmail(authentication.getName()).orElseThrow();
            // 즐찾 누른적 없다면
            if (!boardUtilService.hasFavoriteBoard(board, users)) {
                board.increaseFavoriteCnt();
                BoardFavorite boardFavorite = new BoardFavorite(board, users);
                boardFavoriteRepository.save(boardFavorite);
            } else {    // 즐찾 누른적 있다면
                board.decreaseFavoriteCnt();
                // 만약 db에 데이터가 있어야 되는데 없다면 FavoriteNotFoundException 발생
                BoardFavorite boardFavorite = boardFavoriteRepository.findByBoardAndUsers(board, users).orElseThrow(
                        FavoriteNotFoundException::new);
                boardFavoriteRepository.delete(boardFavorite);
            }
        }
    }

}
