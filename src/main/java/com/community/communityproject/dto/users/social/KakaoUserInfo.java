package com.community.communityproject.dto.users.social;

import java.util.Map;

public class KakaoUserInfo implements OAuth2UserInfo {
    private String providerId;
    private Map<String, Object> kakaoAccount;
    private Map<String, Object> profile;

    public KakaoUserInfo(Map<String, Object> kakaoAccount, String providerId) {
        this.kakaoAccount = kakaoAccount;
        this.providerId = providerId;
        profile = (Map<String, Object>) kakaoAccount.get("profile");
    }

    @Override
    public String getProviderId() {
        return providerId;
    }

    @Override
    public String getProvider() {
        return "kakao";
    }

    @Override
    public String getEmail() {
        if (!(boolean) kakaoAccount.get("email_needs_agreement"))
            return "kakaoUser" + getProviderId();
        return String.valueOf(kakaoAccount.get("email"));
    }

    @Override
    public String getName() {
        return String.valueOf(profile.get("nickname"));
    }

    @Override
    public String getProfileUrl() {
        return String.valueOf(profile.get("profile_image_url"));
    }
}
