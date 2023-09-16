package com.community.communityproject.service.util;

import com.community.communityproject.config.AmazonS3ResourceStorage;
import com.community.communityproject.config.exception.BoardNotFoundException;
import com.community.communityproject.config.exception.UserNotFoundException;
import com.community.communityproject.dto.board.BoardDTOInterface;
import com.community.communityproject.entity.board.Board;
import com.community.communityproject.entity.board.BoardImage;
import com.community.communityproject.entity.board.Category;
import com.community.communityproject.entity.users.Users;
import com.community.communityproject.repository.*;
import jakarta.persistence.criteria.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.Serial;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class BoardUtilService {

    private final BoardRepository boardRepository;
    private final UserRepository userRepository;
    private final BoardLikeRepository boardLikeRepository;
    private final BoardFavoriteRepository boardFavoriteRepository;
    private final BoardImageRepository boardImageRepository;
    private final AmazonS3ResourceStorage amazonS3ResourceStorage;

    private final String BOARD_PATH = "boardImage/";

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
            case "FAVORITE_COUNT" -> "favoriteCnt";
            default -> "regDate";
        };
    }

    /**
     * 공지 게시글이 어떤 카테고리를 가지고 있는지
     * @param bid
     * @return notices
     */
    public Set<Category> getNoticeCategories(Long bid) {
        Board board = boardRepository.getReferenceById(bid);
        return board.getNotices();
    }

    /**
     * LIKE인지 DISLIKE인지 판별
     * likeStatus 필드의 자료형이 boolean이 아닌 이유는
     * 회원이 아예 추천을 안한 게시글을 방문했을 때의 경우를 판단하기 힘들어서
     * @return null 아니면 enum 타입의 LIKE or DISLIKE
     */
    public String checklikeStatus(Long bid) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        Optional<Users> users = userRepository.findByEmail(email);
        Users user = new Users();
        if (users.isEmpty()) {
            // 로그인 안했으면 null 리턴
            return null;
        } else {
            user = users.get();
        }
        Board board = boardRepository.findById(bid).orElseThrow(BoardNotFoundException::new);
        // 추천이나 비추를 누른 기록이 있다면
        if (hasLikeBoard(board, user)) {
            return boardLikeRepository.findByBoardAndUsers(board, user).get().getLikeStatus().toString();
        }
        // 없으면 null
        return null;
    }

    /**
     * 일단 유저가 현재 있는 게시글을 추천이든 비추든 눌렀는지 확인함 뭐든 일단 눌렀으면
     * true 아니면 false
     * @param board
     * @param users
     * @return true or false
     */
    public boolean hasLikeBoard(Board board, Users users) {
        return boardLikeRepository.findByBoardAndUsers(board, users).isPresent();
    }

    /**
     * 유저가 즐찾을 눌렀는지 안눌렀는지
     * @param board
     * @param users
     * @return true or false
     */
    public boolean hasFavoriteBoard(Board board, Users users) {
        return boardFavoriteRepository.findByBoardAndUsers(board, users).isPresent();
    }

    /**
     * controller용, [ROLE_USER] 혹은 [ROLE_ADMIN} 권한 확인 후 해야함
     * @param bid
     * @return true or false
     */
    public boolean hasFavoriteBoard(Long bid) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Users users = userRepository.findByEmail(authentication.getName()).orElseThrow(UserNotFoundException::new);
        return boardFavoriteRepository.findByBoard_idAndUsers_id(bid, users.getId()).isPresent();
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

    public Specification<Board> category(String category) {
        return new Specification<Board>() {
            @Override
            public Predicate toPredicate(Root<Board> q, CriteriaQuery<?> query, CriteriaBuilder cb) {
                return cb.equal(q.get("category"), Category.valueOf(category.toUpperCase()));
            }
        };
    }

    public Specification<Board> search(String kw) {
        return new Specification<Board>() {
            @Serial
            private static final long serialVersionUID = 1L;
            @Override
            public Predicate toPredicate(Root<Board> q, CriteriaQuery<?> query, CriteriaBuilder cb) {
                query.distinct(true);   // 중복제거
                Join<Board, Users> u1 = q.join("users", JoinType.LEFT);
                // 추가 예정
                //Join<Board, Comment> c = q.join("commentList", JoinType.LEFT);

                return cb.or(cb.like(q.get("title"), "%" + kw + "%"), // 제목
                        cb.like(q.get("content"), "%" + kw + "%"));   // 내용
                // 추가 예정
                //cb.like(c.get("content"), "%" + kw + "%"),   // 댓글 내용
                // 작성자는 필요하면 추가
                // cb.like(u1.get("username"), "%" + kw + "%")); // 게시글 작성자
            }
        };
    }
}
