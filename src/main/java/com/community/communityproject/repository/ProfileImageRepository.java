package com.community.communityproject.repository;

import com.community.communityproject.entitiy.users.ProfileImage;
import com.community.communityproject.entitiy.users.Users;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProfileImageRepository extends JpaRepository<ProfileImage, Long> {
    ProfileImage findByUsers(Users users);
}
