package com.community.communityproject.entity.users;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

public class UserDetailsImpl implements UserDetails, OAuth2User {

    private final Users users;
    private Map<String, Object> attributes;

    public UserDetailsImpl(Users users) {
        this.users = users;
    }

    public UserDetailsImpl(Users users, Map<String, Object> attributes) {
        this.users = users;
        this.attributes = attributes;
    }

    /**
     * @return
     */
    @Override
    public Map<String, Object> getAttributes() {
        return attributes;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        Collection<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(() -> users.getUserRole().getValue());
        return authorities;
    }

    @Override
    public String getPassword() {
        return users.getPassword();
    }

    @Override
    public String getUsername() {
        return users.getEmail();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    /**
     * @return
     */
    @Override
    public String getName() {
        return null;
    }
}
