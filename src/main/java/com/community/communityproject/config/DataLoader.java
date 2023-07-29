package com.community.communityproject.config;

import com.community.communityproject.entitiy.users.UserRole;
import com.community.communityproject.entitiy.users.Users;
import com.community.communityproject.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataLoader implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        // PasswordEncoder를 사용하여 비밀번호를 인코딩합니다.
        //String encodedPassword = passwordEncoder.encode("1111");
        System.out.println("======================");
        //System.out.println(encodedPassword);
        System.out.println("======================");
        // 인코딩된 비밀번호를 사용하여 초기 사용자 데이터를 삽입합니다.
//        Users users = Users.builder()
//                .username("user01")
//                .email("user01@email.com")
//                .isLogin(false)
//                .userRole(UserRole.USER)
//                .password(passwordEncoder.encode("1111"))
//                .build();
        Users users = new Users();
        users.setEmail("user01@email.com");
        users.setUsername("user01");
        users.setPassword(passwordEncoder.encode("1111"));
        users.setLogin(false);
        users.setUserRole(UserRole.USER);
        userRepository.save(users);

        Users users2 = new Users();
        users2.setEmail("user02@email.com");
        users2.setUsername("user02");
        users2.setPassword(passwordEncoder.encode("2222"));
        users2.setLogin(false);
        users2.setUserRole(UserRole.USER);
        userRepository.save(users2);
    }
}
