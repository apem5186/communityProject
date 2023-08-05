package com.community.communityproject.service.board;

import com.community.communityproject.config.AmazonS3ResourceStorage;
import com.community.communityproject.dto.TokenDTO;
import com.community.communityproject.dto.board.BoardRequestDTO;
import com.community.communityproject.entitiy.board.Board;
import com.community.communityproject.entitiy.board.BoardImage;
import com.community.communityproject.entitiy.board.Category;
import com.community.communityproject.entitiy.users.Users;
import com.community.communityproject.repository.BoardImageRepository;
import com.community.communityproject.repository.BoardRepository;
import com.community.communityproject.repository.UserRepository;
import com.community.communityproject.service.jwt.AuthService;
import com.community.communityproject.service.users.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

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

    public List<Board> getBoardList() {
        return null;
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
}
