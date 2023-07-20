package com.community.communityproject.repository;

import com.community.communityproject.entitiy.users.Users;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<Users, Long> {

    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
}
