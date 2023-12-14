//package com.community.communityproject.config;
//
//import com.amazonaws.services.s3.AmazonS3Client;
//import com.community.communityproject.entity.board.Board;
//import com.community.communityproject.entity.board.Category;
//import com.community.communityproject.entity.users.ProfileImage;
//import com.community.communityproject.entity.users.UserRole;
//import com.community.communityproject.entity.users.Users;
//import com.community.communityproject.repository.BoardRepository;
//import com.community.communityproject.repository.ProfileImageRepository;
//import com.community.communityproject.repository.UserRepository;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.boot.CommandLineRunner;
//import org.springframework.security.crypto.password.PasswordEncoder;
//import org.springframework.stereotype.Component;
//
//import java.nio.file.Paths;
//import java.util.Arrays;
//import java.util.List;
//import java.util.Random;
//

/* h2 사용시 초기 데이터 생성을 위한 클래스 */


//@Slf4j
//@Component
//@RequiredArgsConstructor
//public class DataLoader implements CommandLineRunner {
//
//    private final UserRepository userRepository;
//    private final ProfileImageRepository profileImageRepository;
//    private final PasswordEncoder passwordEncoder;
//    private final BoardRepository boardRepository;
//    private final AmazonS3Client amazonS3Client;
//
//    @Value("${cloud.aws.s3.bucket}")
//    private String bucket;
//
//    @Override
//    public void run(String... args) {
//        String filePath = String.valueOf(amazonS3Client.getUrl(bucket, "image/profileImage/default/profile_default.jpg"));
//
//        long fileSize = 8636L;
//        String originName = "profile_default.jpg";
//
//        String filePath2 = Paths.get("profileImage", "userImg", "profileImage_attach.jpg").toString();
//        long fileSize2 = 14577L;
//        String originName2 = "profileImage_attach.jpg";
//
//        Users adminUsers = new Users();
//        adminUsers.setEmail("admin01@email.com");
//        adminUsers.setUsername("admin01");
//        adminUsers.setPassword(passwordEncoder.encode("0000"));
//        adminUsers.setLogin(false);
//        adminUsers.setUserRole(UserRole.ADMIN);
//
//        ProfileImage adminProfileImage = ProfileImage.builder()
//                .originName(originName)
//                .filePath(filePath)
//                .fileSize(fileSize)
//                .users(adminUsers)
//                .build();
//
//        userRepository.save(adminUsers);
//        profileImageRepository.save(adminProfileImage);
//
//        Users users = new Users();
//        users.setEmail("user01@email.com");
//        users.setUsername("user01");
//        users.setPassword(passwordEncoder.encode("1111"));
//        users.setLogin(false);
//        users.setUserRole(UserRole.USER);
//
//        ProfileImage profileImage = ProfileImage.builder()
//                .originName(originName)
//                .filePath(filePath)
//                .fileSize(fileSize)
//                .users(users)
//                .build();
//
//        userRepository.save(users);
//        profileImageRepository.save(profileImage);
//
//        Users users2 = new Users();
//        users2.setEmail("user02@email.com");
//        users2.setUsername("user02");
//        users2.setPassword(passwordEncoder.encode("2222"));
//        users2.setLogin(false);
//        users2.setUserRole(UserRole.USER);
//
//        ProfileImage profileImage2 = ProfileImage.builder()
//                .originName(originName)
//                .filePath(filePath)
//                .fileSize(fileSize)
//                .users(users2)
//                .build();
//
//        userRepository.save(users2);
//        profileImageRepository.save(profileImage2);
//
//        Users users3 = new Users();
//        users3.setEmail("user03@email.com");
//        users3.setUsername("user03");
//        users3.setPassword(passwordEncoder.encode("3333"));
//        users3.setLogin(false);
//        users3.setUserRole(UserRole.USER);
//
//        ProfileImage profileImage3 = ProfileImage.builder()
//                .originName(originName2)
//                .filePath(filePath2)
//                .fileSize(fileSize2)
//                .users(users3)
//                .build();
//        log.info("************************************");
//        log.info("************************************");
//        log.info("************************************");
//        log.info(filePath2);
//        log.info("************************************");
//        log.info("************************************");
//        log.info("************************************");
//        userRepository.save(users3);
//        profileImageRepository.save(profileImage3);
//        List<Users> usersList = Arrays.asList(users, users2, users3);
//        List<Category> categoryList = Arrays.asList(
//                Category.COMMUNITY, Category.NOTICE, Category.KNOWLEDGE, Category.QUESTIONS);
//        for (int i = 1; i <= 300; i++) {
//            String title = String.format("게시글 테스트 제목:[%03d]", i);
//            String content = "게시글 테스트 내용";
//            Random random = new Random();
//            int[] randomInts = random.ints(4, 0, 150).toArray();
//            Users randomUser = usersList.get(random.nextInt(usersList.size()));
//            Category randomCategory = categoryList.get(random.nextInt(categoryList.size()));
//            Board board = Board.builder()
//                    .category(randomCategory)
//                    .title(title)
//                    .content(content)
//                    .users(randomUser)
//                    .build();
//            board.testBoardEdit(randomInts[0], randomInts[1], randomInts[2], randomInts[3]);
//            boardRepository.save(board);
//            log.info("------------------------------------------------");
//            log.info("" + board.getHits() + board.getLikeCnt() + board.getReviewCnt());
//            Arrays.stream(randomInts).forEach(randomInt -> log.info(""+randomInt));
//            log.info("------------------------------------------------");
//        }
//    }
//}
