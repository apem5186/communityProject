package com.community.communityproject.service.board;

import com.community.communityproject.config.AmazonS3ResourceStorage;
import com.community.communityproject.dto.TokenDTO;
import com.community.communityproject.dto.board.BoardListResponseDTO;
import com.community.communityproject.dto.board.BoardRequestDTO;
import com.community.communityproject.entitiy.board.Board;
import com.community.communityproject.entitiy.board.BoardImage;
import com.community.communityproject.entitiy.board.Category;
import com.community.communityproject.entitiy.users.ProfileImage;
import com.community.communityproject.entitiy.users.Users;
import com.community.communityproject.repository.BoardImageRepository;
import com.community.communityproject.repository.BoardRepository;
import com.community.communityproject.repository.UserRepository;
import com.community.communityproject.service.jwt.AuthService;
import com.community.communityproject.service.users.UserService;
import jakarta.persistence.criteria.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
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
    private final AmazonS3ResourceStorage amazonS3ResourceStorage;

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    private final String BOARD_PATH = "boardImage/";

    /**
     * keyword, category를 이용해 게시글을 리턴함
     * BoardListResponseDTO를 사용
     * @param page
     * @param kw
     * @param category
     * @return boardDTO
     */
    public Page<BoardListResponseDTO.BoardDTO> getBoardListDTO(int page, String kw, String category) {
        List<Sort.Order> sorts = new ArrayList<>();
        sorts.add(Sort.Order.desc("regDate"));
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
        log.info("");
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
            List<MultipartFile> images = boardRequestDTO.getBoardImage();
            if (images != null && !images.isEmpty() && !images.get(0).isEmpty()) {
                List<MultipartFile> multipartFiles = boardRequestDTO.getBoardImage();
                // ex) "boardImage/community"
                String fullPath = BOARD_PATH + boardRequestDTO.getCategory();
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
