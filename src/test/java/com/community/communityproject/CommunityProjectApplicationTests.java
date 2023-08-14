package com.community.communityproject;

import com.community.communityproject.entitiy.board.Board;
import com.community.communityproject.entitiy.board.Category;
import com.community.communityproject.entitiy.users.Users;
import com.community.communityproject.repository.BoardRepository;
import com.community.communityproject.repository.UserRepository;
import com.community.communityproject.service.board.BoardService;
import com.community.communityproject.service.users.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;

import java.util.Random;
import java.util.stream.IntStream;

@SpringBootTest
class CommunityProjectApplicationTests {

    @Autowired
    private BoardRepository boardRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private BoardService boardService;

    @Test
    void contextLoads() {
    }

    @Test
    void createBoards() {
        Users users = userRepository.findByUsername("user01").orElseThrow();
        for (int i = 1; i <= 150; i++) {
            String title = String.format("게시글 테스트 제목:[%03d]", i);
            String content = "게시글 테스트 내용";
            Random random = new Random();
            int[] randomInts = random.ints(3, 0, 150).toArray();
            Board board = Board.TestBuilder()
                    .category(Category.COMMUNITY)
                    .title(title)
                    .content(content)
                    .hits(randomInts[0])
                    .likeCnt(randomInts[1])
                    .reviewCnt(randomInts[2])
                    .users(users)
                    .build();
            boardRepository.save(board);
        }
    }

}
