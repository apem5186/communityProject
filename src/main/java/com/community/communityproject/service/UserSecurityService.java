package com.community.communityproject.service;

import com.community.communityproject.entitiy.users.UserDetailsImpl;
import com.community.communityproject.entitiy.users.Users;
import com.community.communityproject.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserSecurityService implements UserDetailsService {

    private final UserRepository userRepository;

//    private final PasswordEncoder passwordEncoder;
    @Override
    public UserDetailsImpl loadUserByUsername(String username) throws UsernameNotFoundException {
//        if (username.equals("user01")) {
//            Users users = userRepository.findByUsername(username).orElseThrow();
//            users.setPassword(passwordEncoder.encode("1111"));
//            userRepository.save(users);
//            return new UserDetailsImpl(users);
//        }
        Users users = userRepository.findByUsername(username).orElseThrow(
                () -> new UsernameNotFoundException("User not found with username : " + username));
        log.info("USER : " + users.getUsername() + " AND " + users.getPassword());
        return new UserDetailsImpl(users);
    }
}
