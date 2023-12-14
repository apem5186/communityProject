package com.community.communityproject.service.oauth;

import com.community.communityproject.dto.TokenDTO;
import com.community.communityproject.dto.users.social.KakaoUserInfo;
import com.community.communityproject.dto.users.social.OAuth2UserInfo;
import com.community.communityproject.entity.users.*;
import com.community.communityproject.repository.ProfileImageRepository;
import com.community.communityproject.repository.UserRepository;
import com.community.communityproject.service.jwt.AuthService;
import com.community.communityproject.service.util.UsersUtilService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class OAuthService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {


    private final UserRepository userRepository;
    private final ProfileImageRepository profileImageRepository;
    private final AuthService authService;
    private final UsersUtilService usersUtilService;
    private final OAuth2AuthorizedClientService authorizedClientService;


    private static final String KAKAO_USER_INFO_URL = "https://kapi.kakao.com/v2/user/me";
    private static final String KAKAO_OAUTH_TOKEN = "https://kauth.kakao.com/oauth/token";
    private static final String KAKAO_LOGOUT_URL = "https://kapi.kakao.com/v1/user/logout";
    private static final String KAKAO_LOGOUT_URL2 = "https://kauth.kakao.com/oauth/logout";
    private static final String KAKAO_LOGOUT_REDIRECT_URI = "http://localhost:8080/logout";
    private static final String KAKAO_TOKEN_INFO_URL = "https://kapi.kakao.com/v1/user/access_token_info";

    @Value("${spring.security.oauth2.client.registration.kakao.client-id}")
    private String KAKAO_CLIENT_ID;
    @Value("${spring.security.oauth2.client.registration.kakao.redirect-uri}")
    private String KAKAO_REDIRECT_URI;
    @Value("${kakao-admin-key}")
    private String KAKAO_ADMIN_KEY;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2UserService delegate = new DefaultOAuth2UserService();
        OAuth2User oAuth2User = delegate.loadUser(userRequest);
        OAuth2UserInfo oAuth2UserInfo = null;
        String registrationId = userRequest.getClientRegistration()
                .getRegistrationId(); // OAuth 서비스 이름(ex. GitHub, naver, google)

        if (registrationId.equals("kakao")) {
            oAuth2UserInfo = new KakaoUserInfo((Map)oAuth2User.getAttributes().get("kakao_account"),
                    String.valueOf(oAuth2User.getAttributes().get("id")));
        } else {
            log.error("지원하지 않는 로그인 서비스입니다.");
        }

        Optional<Users> usersOptional = userRepository.findByUsername(oAuth2UserInfo.getName());

        Users users;
        if (usersOptional.isPresent()) {
            // 이 서비스로 로그인 해본 유저면 값 집어넣고 리턴
            users = usersOptional.get();
        } else { // 처음이라면 일단 회원가입
            users = new Users();
            BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
            users.setUsername(oAuth2UserInfo.getName());
            users.setEmail(oAuth2UserInfo.getEmail());
            users.setPassword(passwordEncoder.encode("socialUser"));
            users.setUserRole(UserRole.USER);
            users.setLogin(true);
            users.setProvider(Provider.KAKAO);
            users.setProviderId(oAuth2UserInfo.getProviderId());

            ProfileImage profileImage = ProfileImage.builder()
                    .users(users)
                    .filePath(oAuth2UserInfo.getProfileUrl())
                    .originName("kakaoUserProfile")
                    .build();

            userRepository.save(users);
            profileImageRepository.save(profileImage);
        }
        return new UserDetailsImpl(users, oAuth2User.getAttributes());

    }

    public String getKakaoLoginUrl() {
        return "https://kauth.kakao.com/oauth/authorize"
                + "?client_id=" + KAKAO_CLIENT_ID
                + "&redirect_uri=" + KAKAO_REDIRECT_URI
                + "&response_type=code";
        }

    // session에서 refresh token 가져와서 재발급받기
    public String kakaoReissue(HttpSession session) {
            String rt = (String) session.getAttribute("kakao_refreshToken");
            if (rt == null) {
                // session에 rt가 없다면 그냥 로그아웃 시킴
                UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(KAKAO_LOGOUT_URL2)
                        .queryParam("client_id", KAKAO_CLIENT_ID)
                        .queryParam("logout_redirect_uri", KAKAO_LOGOUT_REDIRECT_URI);

                RestTemplate restTemplate = new RestTemplate();

                restTemplate.getForObject(builder.toUriString(), String.class);

                return null;
            }
            RestTemplate restTemplate = new RestTemplate();

            MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
            params.add("grant_type", "refresh_token");
            params.add("client_id", KAKAO_CLIENT_ID);
            params.add("refresh_token", rt);

            HttpHeaders headers = new HttpHeaders();
            headers.add("Content-type","application/x-www-form-urlencoded;charset=utf-8");

            HttpEntity<MultiValueMap<String, String>> httpEntity = new HttpEntity<>(params, headers);
            ResponseEntity<Map> response = restTemplate.postForEntity(KAKAO_OAUTH_TOKEN, httpEntity, Map.class);

            Map<String, Object> responseBody = response.getBody();
            String newAt = (String) responseBody.get("access_token");
            String newRt = (String) responseBody.get("refresh_token");

            session.setAttribute("kakao_accessToken", newAt);
            session.setAttribute("kakao_refreshToken", newRt);

            return newAt;
        }


    public void kakaoLogout(HttpSession session) throws Exception {
        try {
            RestTemplate restTemplate = new RestTemplate();

            String at = (String) session.getAttribute("kakao_accessToken");
            HttpHeaders headers = new HttpHeaders();
            if (!isKakaoAtValid(at)) {
                // isKakaoAtValid가 false이면 리프레시 토큰이 만료된거
                at = kakaoReissue(session);
            }

            if (at != null) {
                headers.add("Authorization", "Bearer " + at);

                HttpEntity<String> entity = new HttpEntity<>(headers);

                ResponseEntity<Map> response = restTemplate.exchange(
                        KAKAO_LOGOUT_URL,
                        HttpMethod.POST,
                        entity,
                        Map.class
                );

                session.removeAttribute("kakao_accessToken");
                session.removeAttribute("kakao_refreshToken");
            }

        } catch (Exception e) {
            throw new Exception("모종의 이유로 실패했습니다.");
        }

    }

    private boolean isKakaoAtValid(String at) {
        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + at);

        HttpEntity<String> httpEntity = new HttpEntity<>("parameters", headers);

        try {
            ResponseEntity<Map> response = restTemplate.exchange(KAKAO_TOKEN_INFO_URL, HttpMethod.GET, httpEntity, Map.class);
            return true;
        } catch (Exception e) {
            // 실패하면 만료된거, 추후 에러메시지를 설정할 수 있음 현재는 그냥 false로 리턴
            return false;
        }
    }

    public boolean isSocialUser() {
        Users users = usersUtilService.getUsers();
        return users.getProvider() != null;
    }

    public LinkedHashMap<String, String> getToken(HttpSession session) {
        LinkedHashMap<String, String> map = new LinkedHashMap<>();
        String at = (String) session.getAttribute("kakao_access_token");
        String rt = (String) session.getAttribute("kakao_refresh_token");
        if (at == null && rt == null) {
            return null;
        }
        map.put("at", at);
        map.put("rt", rt);

        return map;
    }

    public TokenDTO createToken(HttpServletResponse response) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        TokenDTO tokenDTO = authService.generateToken("Server", authentication.getName(), authService.getAuthorities(authentication));
        authService.setCookie(response, tokenDTO.getAccessToken(), tokenDTO.getRefreshToken());

        return tokenDTO;
    }

    public Map<String, String> getKakaoToken(HttpServletRequest request) {
//        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
//        if (authentication instanceof OAuth2AuthenticationToken authenticationToken) {
//
//            OAuth2AuthorizedClient authorizedClient = authorizedClientService.loadAuthorizedClient(
//                    authenticationToken.getAuthorizedClientRegistrationId(),
//                    authenticationToken.getName());
            Users users = usersUtilService.getUsers();
            String clientRegistrationId = String.valueOf(users.getProvider()).toLowerCase();
            OAuth2AuthorizedClient authorizedClient = authorizedClientService.loadAuthorizedClient(
                    clientRegistrationId,
                    users.getEmail() // Get the username from the authentication token
            );

            String kakao_AccessToken = authorizedClient.getAccessToken().getTokenValue();
            String kakao_RefreshToken = authorizedClient.getRefreshToken().getTokenValue();
            HttpSession session = request.getSession(true);
            session.setAttribute("kakao_access_token", kakao_AccessToken);
            session.setAttribute("kakao_refresh_token", kakao_RefreshToken);

            HashMap<String, String> map = new HashMap<>();
            map.put("kakao_access_token", kakao_AccessToken);
            map.put("kakao_refresh_token", kakao_RefreshToken);

            return map;
    }

}
