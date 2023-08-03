package com.community.communityproject.service.board;

import com.community.communityproject.entitiy.board.Board;
import com.community.communityproject.repository.BoardImageRepository;
import com.community.communityproject.repository.BoardRepository;
import com.community.communityproject.repository.UserRepository;
import com.community.communityproject.service.jwt.AuthService;
import com.community.communityproject.service.users.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

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

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    private final String BOARD_PATH = "boardImage/";

    public List<Board> getBoardList() {
        return null;
    }
}
