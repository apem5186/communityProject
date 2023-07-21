package com.community.communityproject.repository;

import com.community.communityproject.entitiy.users.Users;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<Users, Long> {

    boolean existsByUsername(String username);
    boolean existsByEmail(String email);

    Optional<Users> findByUsername(String username);
}
