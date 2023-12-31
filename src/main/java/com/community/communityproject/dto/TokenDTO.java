package com.community.communityproject.dto;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TokenDTO {

    private String accessToken;
    private String refreshToken;

    public TokenDTO(String accessToken, String refreshToken) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
    }

    // 둘 다 있으면 false 반환
    public boolean isEmpty() {
        return accessToken.isEmpty() || refreshToken.isEmpty();
    }
}
