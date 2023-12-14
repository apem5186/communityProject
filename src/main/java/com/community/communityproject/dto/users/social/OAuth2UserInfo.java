package com.community.communityproject.dto.users.social;

public interface OAuth2UserInfo {

    String getProviderId();
    String getProvider();
    String getEmail();
    String getName();
    String getProfileUrl();
}

