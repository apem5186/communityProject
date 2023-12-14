package com.community.communityproject.service.util;

import com.community.communityproject.config.exception.UserNotFoundException;
import com.community.communityproject.entity.users.UserRole;
import com.community.communityproject.entity.users.Users;
import com.community.communityproject.repository.UserRepository;
import jakarta.persistence.criteria.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.io.Serial;

@Slf4j
@Service
@RequiredArgsConstructor
public class UsersUtilService {

    private final UserRepository userRepository;

    public Specification<Users> search(String kw, String searchField) {
        return new Specification<Users>() {
            @Serial
            private static final long serialVersionUID = 1L;
            @Override
            public Predicate toPredicate(Root<Users> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
                query.distinct(true);   // 중복제거
                if (kw == null || kw.isEmpty()) {
                    return null; // 검색이 없을 경우 모든 경우 반환
                }

                Predicate predicate;
                switch (searchField) {
                    case "id" -> predicate = cb.like(cb.toString(root.get("id")), "%" + kw + "%");
                    case "username" -> predicate = cb.like(root.get("username"), "%" + kw + "%");
                    case "email" -> predicate = cb.like(root.get("email"), "%" + kw + "%");
                    case "ALL" ->
                        predicate = cb.or(cb.like(root.get("username"), "%" + kw + "%"),
                                cb.like(root.get("email"), "%" + kw + "%"),
                                cb.like(cb.toString(root.get("id")), "%" + kw + "%"));
                    default -> throw new IllegalArgumentException("Unknown search field");
                }
                return predicate;
            }
        };
    }

    public Specification<Users> filterByUserRole(String userRole) {
        return (root, query, cb) -> cb.equal(root.get("userRole"), UserRole.valueOf(userRole));
    }

    public Specification<Users> filterByOption2(String option2) {
        if (option2.equals("LOGIN"))
            return (root, query, cb) -> cb.isTrue(root.get("isLogin"));
        else if (option2.equals("LOGOUT"))
            return (root, query, cb) -> cb.isFalse(root.get("isLogin"));
        else return null;
    }

    /**
     * SecurityContextHolder를 이용해 Users를 찾음
     * @return Users
     */
    public Users getUsers() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        return userRepository.findByEmail(email).orElseThrow(UserNotFoundException::new);
    }

    public boolean isLogin() {
        return getUsers().isLogin();
    }

}
