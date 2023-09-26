package com.community.communityproject.repository;

import com.community.communityproject.entity.users.Users;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<Users, Long> {

    boolean existsByUsername(String username);
    boolean existsByEmail(String email);

    Optional<Users> findByUsername(String username);

    Optional<Users> findByEmail(String email);
    Page<Users> findAll(Specification<Users> spec, Pageable pageable);
}
