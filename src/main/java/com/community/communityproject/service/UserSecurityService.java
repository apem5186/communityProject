package com.community.communityproject.service;

import com.community.communityproject.entitiy.users.UserDetailsImpl;
import com.community.communityproject.entitiy.users.Users;
import com.community.communityproject.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserSecurityService implements UserDetailsService {

    private final UserRepository userRepository;


    @Override
    public UserDetailsImpl loadUserByUsername(String email) throws UsernameNotFoundException {
        Users users = userRepository.findByEmail(email).orElseThrow(
                () -> new UsernameNotFoundException("User not found with email : " + email));
        log.info("USER : " + users.getEmail() + " AND " + users.getPassword());
        return new UserDetailsImpl(users);
    }
}
