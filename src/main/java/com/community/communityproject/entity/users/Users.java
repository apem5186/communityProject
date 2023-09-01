package com.community.communityproject.entity.users;

import com.community.communityproject.entity.BaseEntity;
import com.community.communityproject.entity.board.BoardFavorite;
import com.community.communityproject.entity.board.BoardLike;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

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

    @OneToOne(mappedBy = "users", fetch = FetchType.LAZY, cascade = CascadeType.REMOVE)
    private ProfileImage profileImage;

    @OneToMany(mappedBy = "users", cascade = CascadeType.REMOVE)
    private List<BoardFavorite> favoriteBoardsByUser = new ArrayList<>();

    @OneToMany(mappedBy = "users", cascade = CascadeType.REMOVE)
    private List<BoardLike> likeBoardsByUser = new ArrayList<>();

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
