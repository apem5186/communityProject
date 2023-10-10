package com.community.communityproject.service.oauth;

import com.community.communityproject.dto.TokenDTO;
import com.community.communityproject.entity.users.ProfileImage;
import com.community.communityproject.entity.users.SocialUser;
import com.community.communityproject.entity.users.UserRole;
import com.community.communityproject.entity.users.Users;
import com.community.communityproject.repository.ProfileImageRepository;
import com.community.communityproject.repository.UserRepository;
import com.community.communityproject.service.jwt.AuthService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class OAuthService {


    private final UserRepository userRepository;
    private final ProfileImageRepository profileImageRepository;
    private final AuthService authService;
    private static final String KAKAO_USER_INFO_URL = "https://kapi.kakao.com/v2/user/me";

    // TODO : redirect-uri에서 나온 access token 그대로 쓰는게 아니라 https://kauth.kakao.com/oauth/token
    //        여기서 토큰 또 받아서 그걸로 유저 정보 가져와야 함
    public TokenDTO kakaoLogin(String at, HttpSession session) {
        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + at);
        log.info("at : " + at);
        HttpEntity<String> entity = new HttpEntity<>(null, headers);

        ResponseEntity<Map<String, Object>> response = restTemplate.exchange(KAKAO_USER_INFO_URL, HttpMethod.GET, entity,
                new ParameterizedTypeReference<Map<String, Object>>() {});
        Object kakaoAccountObj = response.getBody().get("kakao_account");
        String kakaoUserId = (String) response.getBody().get("id");
        if (kakaoAccountObj instanceof Map) {
            Map<String, Object> kakaoAccount = (Map<String, Object>) kakaoAccountObj;
            String email = "kakaoUser";

            // 만약 이메일 정보 동의를 안한 사람이면 카톡 고유 id를 붙임
            if (((boolean) kakaoAccount.get("email_needs_agreement")))
                email = ((String) kakaoAccount.get("email")) + kakaoUserId;

            Map<String, Object> profile = (Map<String, Object>) kakaoAccount.get("profile");
            String username = (String) profile.get("nickname");
            String profileImageUrl = (String) profile.get("profile_image_url");
            if (userRepository.findByUsername(username).isEmpty()) {
                Users users = new Users();
                users.setUsername(username);
                users.setEmail(email);
                users.setPassword("socialUser");
                users.setUserRole(UserRole.USER);
                users.setLogin(true);
                users.setSocialUser(SocialUser.KAKAO);

                ProfileImage profileImage = ProfileImage.builder()
                        .users(users)
                        .filePath(profileImageUrl)
                        .build();

                userRepository.save(users);
                profileImageRepository.save(profileImage);
            }

            // 로그아웃 할 때 필요함
            session.setAttribute("kakao_accessToken", at);


            return authService.socialLogin(email, "socialUser");

        } else {
            throw new IllegalArgumentException("Expected a Map for 'kakao_account, but found: " + kakaoAccountObj);
        }
    }
}
