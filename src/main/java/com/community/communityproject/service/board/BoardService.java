package com.community.communityproject.service.board;

import com.amazonaws.services.s3.AmazonS3Client;
import com.community.communityproject.config.AmazonS3ResourceStorage;
import com.community.communityproject.config.exception.BoardNotFoundException;
import com.community.communityproject.dto.TokenDTO;
import com.community.communityproject.dto.board.BoardDTOInterface;
import com.community.communityproject.dto.board.BoardEditRequestDTO;
import com.community.communityproject.dto.board.BoardListResponseDTO;
import com.community.communityproject.dto.board.BoardRequestDTO;
import com.community.communityproject.entitiy.board.Board;
import com.community.communityproject.entitiy.board.BoardImage;
import com.community.communityproject.entitiy.board.BoardLike;
import com.community.communityproject.entitiy.board.Category;
import com.community.communityproject.entitiy.users.ProfileImage;
import com.community.communityproject.entitiy.users.UserRole;
import com.community.communityproject.entitiy.users.Users;
import com.community.communityproject.repository.BoardImageRepository;
import com.community.communityproject.repository.BoardLikeRepository;
import com.community.communityproject.repository.BoardRepository;
import com.community.communityproject.repository.UserRepository;
import com.community.communityproject.service.jwt.AuthService;
import com.community.communityproject.service.jwt.TokenProvider;
import com.community.communityproject.service.users.UserService;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.persistence.criteria.*;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.catalina.Role;
import org.apache.catalina.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.endpoint.InvalidEndpointRequestException;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
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

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    private final String BOARD_PATH = "boardImage/";

    /**
     * 한 게시글의 데이터를 가져오기 위한 service
     * @param bid
     * @return BoardListResponseDTO.BoardDTO
     */
    @Transactional
    public BoardListResponseDTO.BoardDTO getBoard(Long bid) {
        Board board = boardRepository.getReferenceById(bid);
        if (board.getContent().isEmpty()) {
            throw new BoardNotFoundException();
        }
        return new BoardListResponseDTO().new BoardDTO(board);
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
        sort = sort(sort);
        sorts.add(Sort.Order.desc(sort));
        Pageable pageable = PageRequest.of(page-1, 10, Sort.by(sorts));
        Specification<Board> spec = search(kw);
        spec = spec.and(category(category));
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
        Specification<Board> spec = category(category);
        Page<Board> boards =  boardRepository.findAll(spec, pageable);

        // Convert each Board entity to BoardDTO
        return boards.getContent().stream()
                .map(board -> new BoardListResponseDTO().getBoardDTO(board))
                .collect(Collectors.toList());
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
        log.info("=============================================");
        log.info("=============================================");
        log.info("=============================================");
        TokenDTO tokenDTO = authService.validateToken(response, request);
        if (tokenDTO != null) {
            Users users = userRepository.findByEmail(boardRequestDTO.getEmail()).orElseThrow();
            Board board = Board.builder()
                    .title(boardRequestDTO.getTitle())
                    .content(boardRequestDTO.getContent())
                    .category(Category.valueOf(boardRequestDTO.getCategory().toUpperCase()))
                    .users(users)
                    .build();
            // db에 이미지 넣을라면 게시글 정보 먼저 넣어야 함
            boardRepository.save(board);
            postS3(boardRequestDTO, board);
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
                String path = trimUrlToPath(boardImage.getFilePath());
                if (amazonS3Client.doesObjectExist(bucket, path)) {
                    amazonS3Client.deleteObject(bucket, path);
                }
                boardImageRepository.delete(boardImage);
            }
            // 저장할 이미지가 없다면 board만 수정
            Board board = boardRepository.getReferenceById(boardEditRequestDTO.getBid());
            board.edit(boardEditRequestDTO.getTitle(), boardEditRequestDTO.getContent(),
                    boardEditRequestDTO.getCategory().toUpperCase());
            boardRepository.save(board);
            // 저장할 이미지가 있다면 s3에 올림
            if (!boardEditRequestDTO.getBoardImage().isEmpty()) {
                postS3(boardEditRequestDTO, board);
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
                String path = trimUrlToPath(boardImage.getFilePath());
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
     * @param id
     */
    @Transactional
    public void updateHits(Integer id) {
        this.boardRepository.updateHits(id);
    }

    /**
     * 게시글 추천, 비추천 로직
     * @param id
     * @param response
     * @param request
     * @param status
     */
    @Transactional
    public void likeBoard(Long id, HttpServletResponse response, HttpServletRequest request, boolean status) {
        TokenDTO tokenDTO = authService.validateToken(response, request);
        if (tokenDTO != null) {
            // 일단 Board랑 Users 가져옴
            Board board = boardRepository.getReferenceById(id);
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            Users users = userRepository.findByEmail(authentication.getName()).orElseThrow();
            // 추천 버튼을 눌렀으면
            if (status) {
                // url 조작으로 추천 누른 상태인데 비추 요청 했을 때
                if (isRecommend() != null && isRecommend().equals("unrecommended")) {
                    throw new InvalidEndpointRequestException("Invalid Endpoint", "추천 버튼을 누른 상태에서 비추 요청을 했습니다.");
                }
                // 추천 버튼을 누른적이 없을 때
                if (boardLikeRepository.findByBoardAndUsers(board, users) == null) {
                    board.increaseLikeCnt();
                    BoardLike boardLike = new BoardLike(board, users, "recommend");
                    boardLikeRepository.save(boardLike);
                } else {    // 있을 때
                    board.decreaseLikeCnt();
                    BoardLike boardLike = boardLikeRepository.findByBoardAndUsers(board, users);
                    boardLikeRepository.delete(boardLike);
                }
            } else {    // 비추천 버튼을 눌렀으면
                // url 조작으로 비추 누른 상탠데 추천 요청 했을 때
                if (isRecommend() != null && isRecommend().equals("recommend")) {
                    throw new InvalidEndpointRequestException("Invalid Endpoint", "비추 버튼을 누른 상태에서 추천 요청을 했습니다.");
                }
                // 비추천 버튼을 누른적이 없을 때
                if (boardLikeRepository.findByBoardAndUsers(board, users) == null) {
                    board.decreaseLikeCnt();
                    BoardLike boardLike = new BoardLike(board, users, "unrecommended");
                    boardLikeRepository.save(boardLike);
                } else {    // 있을 때
                    board.increaseLikeCnt();
                    BoardLike boardLike = boardLikeRepository.findByBoardAndUsers(board, users);
                    boardLikeRepository.delete(boardLike);
                }
            }
        }
    }

    /**
     * 정렬 기준 "LATEST"는 default로 "regDate"가 됨
     * @param sort
     * @return result
     */
    public String sort(String sort) {
        return switch (sort) {
            case "VOTE_COUNT" -> "likeCnt";
            case "COMMENT_COUNT" -> "reviewCnt";
            case "HITS_COUNT" -> "hits";
            default -> "regDate";
        };
    }

    /**
     * recommend인지 unrecommended인지 판별
     * isRecommend 필드의 자료형이 boolean이 아닌 이유는
     * 회원이 아예 추천을 안한 게시글을 방문했을 때의 경우를 판단하기 힘들어서
     * @return null 아니면 recommend or unrecommended
     */
    public String isRecommend() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        Users users = userRepository.findByEmail(email).orElseThrow();
        if (boardLikeRepository.findByUsers(users).isPresent()) {
            return boardLikeRepository.findByUsers(users).get().getIsRecommend();
        }
        return null;
    }
    /**
     * filePath를 bucket url을 짤라서 반환함
     * @param fullUrl
     * @return path
     */
    public String trimUrlToPath(String fullUrl) {
        int imageIndex = fullUrl.indexOf("/image");

        if(imageIndex != -1) {
            return fullUrl.substring(imageIndex + 1);
        }
        return fullUrl; // "/image"가 없는 경우 원래 URL 반환
    }

    /**
     * s3에 board 이미지를 업로드 editBoard와 postBoard에 쓰임
     * @param dto
     * @param board
     */
    public void postS3(BoardDTOInterface dto, Board board) {
        List<MultipartFile> images = dto.getBoardImage();
        if (images != null && !images.isEmpty() && !images.get(0).isEmpty()) {
            List<MultipartFile> multipartFiles = dto.getBoardImage();
            // ex) "boardImage/community"
            String fullPath = BOARD_PATH + dto.getCategory();
            for (MultipartFile multipartFile:
                    multipartFiles) {
                String filePath = amazonS3ResourceStorage.store(fullPath, multipartFile);
                BoardImage boardImage = BoardImage.builder()
                        .originFilename(multipartFile.getOriginalFilename())
                        .filePath(filePath)
                        .fileSize(multipartFile.getSize())
                        .board(board)
                        .build();

                boardImageRepository.save(boardImage);
            }
        }
    }
    private Specification<Board> category(String category) {
        return new Specification<Board>() {
            @Override
            public Predicate toPredicate(Root<Board> q, CriteriaQuery<?> query, CriteriaBuilder cb) {
                return cb.equal(q.get("category"), Category.valueOf(category.toUpperCase()));
            }
        };
    }

    private Specification<Board> search(String kw) {
        return new Specification<Board>() {
            private static final long serialVersionUID = 1L;
            @Override
            public Predicate toPredicate(Root<Board> q, CriteriaQuery<?> query, CriteriaBuilder cb) {
                query.distinct(true);   // 중복제거
                Join<Board, Users> u1 = q.join("users", JoinType.LEFT);
                // 추가 예정
                //Join<Board, Comment> c = q.join("commentList", JoinType.LEFT);

                return cb.or(cb.like(q.get("title"), "%" + kw + "%"), // 제목
                        cb.like(q.get("content"), "%" + kw + "%"),   // 내용
                        // 추가 예정
                        //cb.like(c.get("content"), "%" + kw + "%"),   // 댓글 내용
                        cb.like(u1.get("username"), "%" + kw + "%")); // 게시글 작성자
            }
        };
    }
}
