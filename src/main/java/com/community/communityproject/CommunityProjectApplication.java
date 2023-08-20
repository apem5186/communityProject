package com.community.communityproject;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;

@SpringBootApplication
@EntityScan(basePackages = "com.community.communityproject.entity")
public class CommunityProjectApplication {

    public static void main(String[] args) {
        SpringApplication.run(CommunityProjectApplication.class, args);
    }

}
