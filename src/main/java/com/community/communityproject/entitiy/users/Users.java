package com.community.communityproject.entitiy.users;

import com.community.communityproject.entitiy.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table
public class Users extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false, unique = true)
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserRole userRole;

    @Column
    private boolean isLogin = false;

    public Users(Long id, String username, String password, String email,
                UserRole userRole, boolean isLogin) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.email = email;
        this.userRole = userRole;
        this.isLogin = isLogin;
    }

    public static Users build(Users users) {

        return new Users(
                users.getId(),
                users.getUsername(),
                users.getPassword(),
                users.getEmail(),
                users.getUserRole(),
                users.isLogin
        );
    }
}
