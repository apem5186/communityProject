package com.community.communityproject.service.jwt;

import com.community.communityproject.dto.TokenDTO;
import com.community.communityproject.dto.UsersLoginDTO;
import com.community.communityproject.entitiy.users.Users;
import com.community.communityproject.repository.UserRepository;
import com.community.communityproject.service.redis.RedisService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 출처 : https://velog.io/@u-nij/JWT-JWT-%EA%B5%AC%ED%98%84%ED%95%98%EA%B8%B0Feat.-Redis-6-DtoAuthDto-ServiceUserService-AuthService
 */
@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final TokenProvider tokenProvider;
    private final RedisService redisService;
    private final AuthenticationManagerBuilder authenticationManagerBuilder;

    private final String SERVER = "Server";

    public TokenDTO login(UsersLoginDTO usersLoginDTO) {
        Optional<Users> users = userRepository.findByEmail(usersLoginDTO.getEmail());
        if (users.isEmpty()) {
            return null;
        } else {
            log.info("== users 정보 : " + users.get().getEmail());
            users.get().setLogin(true);
            userRepository.save(users.get());
        }
        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(usersLoginDTO.getEmail(), usersLoginDTO.getPassword());

        Authentication authentication = authenticationManagerBuilder.getObject()
                .authenticate(authenticationToken);
        SecurityContextHolder.getContext().setAuthentication(authentication);
        return generateToken(SERVER, authentication.getName(), getAuthorities(authentication));
    }

    // AT가 만료일자만 초과한 유효한 토큰인지 검사
    public boolean validate(String requestAccessTokenInHeader) {
        String requestAccessToken = resolveToken(requestAccessTokenInHeader);
        return tokenProvider.validateAccessTokenOnlyExpired(requestAccessToken); // true = 재발급
    }


    // 토큰 재발급: validate 메서드가 true 반환할 때만 사용 -> AT, RT 재발급
    @Transactional
    public TokenDTO reissue(String requestAccessTokenInHeader, String requestRefreshToken) {
        String requestAccessToken = resolveToken(requestAccessTokenInHeader);

        Authentication authentication = tokenProvider.getAuthentication(requestAccessToken);
        String principal = getPrincipal(requestAccessToken);

        String refreshTokenInRedis = redisService.getValues("RT(" + SERVER + "):" + principal);
        if (refreshTokenInRedis == null) { // Redis에 저장되어 있는 RT가 없을 경우
            return null; // -> 재로그인 요청
        }

        // 요청된 RT의 유효성 검사 & Redis에 저장되어 있는 RT와 같은지 비교
        if(!tokenProvider.validateRefreshToken(requestRefreshToken) || !refreshTokenInRedis.equals(requestRefreshToken)) {
            redisService.deleteValues("RT(" + SERVER + "):" + principal); // 탈취 가능성 -> 삭제
            return null; // -> 재로그인 요청
        }

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String authorities = getAuthorities(authentication);

        // 토큰 재발급 및 Redis 업데이트
        redisService.deleteValues("RT(" + SERVER + "):" + principal); // 기존 RT 삭제
        TokenDTO tokenDto = tokenProvider.createToken(principal, authorities);
        saveRefreshToken(SERVER, principal, tokenDto.getRefreshToken());
        return tokenDto;
    }


    // 토큰 발급
    @Transactional
    public TokenDTO generateToken(String provider, String email, String authorities) {
        // RT가 이미 있을 경우
        if(redisService.getValues("RT(" + provider + "):" + email) != null) {
            redisService.deleteValues("RT(" + provider + "):" + email); // 삭제
        }

        // AT, RT 생성 및 Redis에 RT 저장
        TokenDTO tokenDTO = tokenProvider.createToken(email, authorities);
        saveRefreshToken(provider, email, tokenDTO.getRefreshToken());
        return tokenDTO;
    }

    // RT를 Redis에 저장
    @Transactional
    public void saveRefreshToken(String provider, String principal, String refreshToken) {
        redisService.setValuesWithTimeout("RT(" + provider + "):" + principal, // key
                refreshToken, // value
                tokenProvider.getTokenExpirationTime(refreshToken)); // timeout(milliseconds)
    }

    // 권한 이름 가져오기
    public String getAuthorities(Authentication authentication) {
        return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(","));
    }

    // AT로부터 principal 추출
    public String getPrincipal(String requestAccessToken) {
        return tokenProvider.getAuthentication(requestAccessToken).getName();
    }

    // "Bearer {AT}"에서 {AT} 추출
    public String resolveToken(String requestAccessTokenInHeader) {
        if (requestAccessTokenInHeader != null && requestAccessTokenInHeader.startsWith("Bearer ")) {
            return requestAccessTokenInHeader.substring(7);
        }
        return null;
    }

    // 로그아웃
    @Transactional
    public void logout(String requestAccessTokenInHeader) {
        String requestAccessToken = resolveToken(requestAccessTokenInHeader);
        String principal = getPrincipal(requestAccessToken);

        // isLogin -> false
        Users users = userRepository.findByUsername(principal).orElseThrow();
        users.setLogin(false);
        userRepository.save(users);

        // Redis에 저장되어 있는 RT 삭제
        String refreshTokenInRedis = redisService.getValues("RT(" + SERVER + "):" + principal);
        if (refreshTokenInRedis != null) {
            redisService.deleteValues("RT(" + SERVER + "):" + principal);
        }

        // Redis에 로그아웃 처리한 AT 저장
        long expiration = tokenProvider.getTokenExpirationTime(requestAccessToken) - new Date().getTime();
        redisService.setValuesWithTimeout(requestAccessToken,
                "logout",
                expiration);

        // SecurityContextHolder의 user 정보 삭제
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null) {
            SecurityContextHolder.clearContext();
        }
    }

}
