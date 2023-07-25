package com.community.communityproject.service;

import com.community.communityproject.dto.UsersInfo;
import com.community.communityproject.entitiy.users.UserRole;
import com.community.communityproject.entitiy.users.Users;
import com.community.communityproject.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;

    @Transactional
    public void signup(String username, String email, String password) {
        Users users = new Users();
        users.setUsername(username);
        users.setEmail(email);
        users.setPassword(passwordEncoder.encode(password));
        users.setUserRole(UserRole.USER);
        userRepository.save(users);
    }

    public UsersInfo loadUser(String email) {
        Users users = userRepository.findByEmail(email).orElseThrow();
        UsersInfo usersInfo = UsersInfo.builder()
                .username(users.getUsername())
                .email(users.getEmail())
                .userRole(users.getUserRole())
                .build();
        return usersInfo;
    }

}
