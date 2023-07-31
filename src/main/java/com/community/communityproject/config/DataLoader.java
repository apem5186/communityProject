package com.community.communityproject.config;

import com.community.communityproject.entitiy.users.ProfileImage;
import com.community.communityproject.entitiy.users.UserRole;
import com.community.communityproject.entitiy.users.Users;
import com.community.communityproject.repository.ProfileImageRepository;
import com.community.communityproject.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.nio.file.Paths;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataLoader implements CommandLineRunner {

    private final UserRepository userRepository;
    private final ProfileImageRepository profileImageRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {

        String filePath;
        filePath = Paths.get("profileImage", "default", "profile_default.jpg").toString();
        long fileSize = 8636L;
        String originName = "profile_default.jpg";

        String filePath2 = Paths.get("profileImage", "userImg", "profileImage_attach.jpg").toString();
        long fileSize2 = 14577L;
        String originName2 = "profileImage_attach.jpg";


        Users users = new Users();
        users.setEmail("user01@email.com");
        users.setUsername("user01");
        users.setPassword(passwordEncoder.encode("1111"));
        users.setLogin(false);
        users.setUserRole(UserRole.USER);

        ProfileImage profileImage = ProfileImage.builder()
                .originName(originName)
                .filePath(filePath)
                .fileSize(fileSize)
                .users(users)
                .build();

        userRepository.save(users);
        profileImageRepository.save(profileImage);

        Users users2 = new Users();
        users2.setEmail("user02@email.com");
        users2.setUsername("user02");
        users2.setPassword(passwordEncoder.encode("2222"));
        users2.setLogin(false);
        users2.setUserRole(UserRole.USER);

        ProfileImage profileImage2 = ProfileImage.builder()
                .originName(originName)
                .filePath(filePath)
                .fileSize(fileSize)
                .users(users2)
                .build();

        userRepository.save(users2);
        profileImageRepository.save(profileImage2);

        Users users3 = new Users();
        users3.setEmail("user03@email.com");
        users3.setUsername("user03");
        users3.setPassword(passwordEncoder.encode("3333"));
        users3.setLogin(false);
        users3.setUserRole(UserRole.USER);

        ProfileImage profileImage3 = ProfileImage.builder()
                .originName(originName2)
                .filePath(filePath2)
                .fileSize(fileSize2)
                .users(users3)
                .build();
        log.info("************************************");
        log.info("************************************");
        log.info("************************************");
        log.info(filePath2);
        log.info("************************************");
        log.info("************************************");
        log.info("************************************");
        userRepository.save(users3);
        profileImageRepository.save(profileImage3);
    }
}
