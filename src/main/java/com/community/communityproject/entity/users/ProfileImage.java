package com.community.communityproject.entity.users;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Entity
@NoArgsConstructor
@Table(name = "profile_image")
public class ProfileImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "pid")
    private Long piId;  // Profile Image Id

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "users_id")
    private Users users;

    private String originName;  // 파일 원본명

    private String filePath;    // 파일 경로

    private Long fileSize;  // 파일 사이즈

    @Builder
    public ProfileImage(String originName, String filePath, Long fileSize, Users users) {
        this.originName = originName;
        this.filePath = filePath;
        this.fileSize = fileSize;
        this.users = users;
    }
}
