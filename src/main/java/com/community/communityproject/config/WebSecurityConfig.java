package com.community.communityproject.config;

import com.community.communityproject.config.filter.JwtAuthenticationFilter;
import com.community.communityproject.config.handler.*;
import com.community.communityproject.repository.UserRepository;
import com.community.communityproject.service.users.UserSecurityService;
import com.community.communityproject.service.jwt.AuthService;
import com.community.communityproject.service.jwt.TokenProvider;
import com.community.communityproject.service.redis.RedisService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.header.writers.frameoptions.XFrameOptionsHeaderWriter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import java.util.Collection;
import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
@RequiredArgsConstructor
public class WebSecurityConfig {
    
    private final UserRepository userRepository;
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    private final JwtAccessDeniedHandler jwtAccessDeniedHandler;
    private final TokenProvider tokenProvider;
    private final AuthService authService;
    private final RedisService redisService;
    private final AuthenticationManagerBuilder authenticationManagerBuilder;
    private final UserSecurityService userSecurityService;


    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        return web -> web.ignoring().requestMatchers("/h2-console/**", "/favicon.ico",
                "/css/**", "/js/**", "/img/**", "/signup/checkUsername")
                .requestMatchers(PathRequest.toStaticResources().atCommonLocations());  // 정적 리소스
    }
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
                // csrf 비활성화
                .csrf(AbstractHttpConfigurer::disable)
                // 비인증시 로그인 화면으로 이동됨, 사용안함
                .httpBasic(AbstractHttpConfigurer::disable)
                // REST API 기능을 이용할 것이므로 사용안함
                .formLogin(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests((authorizeHttpRequests) ->
                        authorizeHttpRequests
                                .requestMatchers("/").permitAll()
                                .requestMatchers("/fragments").permitAll()
                                .requestMatchers("/login").permitAll()
                                .requestMatchers("/signup").permitAll()
                                .requestMatchers("/signup/checkUsername").permitAll()
                                .requestMatchers("/h2-console").permitAll()
                                .requestMatchers("/community/new", "/notice/new", "/knowledge/new", "/questions/new").hasAnyRole("USER", "ADMIN")
                                .requestMatchers(new AntPathRequestMatcher("/kakao/callback/**")).permitAll()
                                .requestMatchers(new AntPathRequestMatcher("/community/**")).permitAll()
                                .requestMatchers(new AntPathRequestMatcher("/notice/**")).permitAll()
                                .requestMatchers(new AntPathRequestMatcher("/knowledge/**")).permitAll()
                                .requestMatchers(new AntPathRequestMatcher("/questions/**")).permitAll()
                                .requestMatchers(new AntPathRequestMatcher("/h2-console/**")).permitAll()
                                .requestMatchers(new AntPathRequestMatcher("/profileImage/**")).permitAll()
                                .requestMatchers(new AntPathRequestMatcher("/profileImage/userImg/**")).permitAll()
                                .requestMatchers(new AntPathRequestMatcher("/profile/**")).permitAll()
                                .requestMatchers(new AntPathRequestMatcher("/get/**")).permitAll()
                                .requestMatchers(new AntPathRequestMatcher("/favicon.ico")).permitAll()
                                .requestMatchers(new AntPathRequestMatcher("/css/**")).permitAll()
                                .requestMatchers(new AntPathRequestMatcher("/js/**")).permitAll()
                                .requestMatchers(new AntPathRequestMatcher("/img/**")).permitAll()
                                .requestMatchers(new AntPathRequestMatcher("/ac/**")).hasRole("ADMIN")
                                .anyRequest().authenticated()
                                )
                // 예외 처리
//                .exceptionHandling((exceptionHandling) ->
//                        exceptionHandling
//                                .authenticationEntryPoint(jwtAuthenticationEntryPoint)  // 인가 실패
//                                .accessDeniedHandler(jwtAccessDeniedHandler))   // 인증 실패
                .headers((headers) ->
                        headers
                                .addHeaderWriter(new XFrameOptionsHeaderWriter(
                                        XFrameOptionsHeaderWriter.XFrameOptionsMode.SAMEORIGIN
                                )))
//                .formLogin((formLogin) ->
//                        formLogin
//                                .loginPage("/login")
//                                .failureHandler(new CustomAuthenticationFailureHandler())
//                                .successHandler(new CustomAuthenticationSuccessHandler(userRepository,
//                                        authenticationManagerBuilder, authService))
//                                .permitAll()
//                                )
                .addFilterBefore(new JwtAuthenticationFilter(tokenProvider, userSecurityService), UsernamePasswordAuthenticationFilter.class)
                // 세션 관리
                .sessionManagement((sessionManagement) ->
                        sessionManagement
                                .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // TODO : 순환참조 및 추후 추가할 OAUTH2 등을 위해 logout은 controller 형식으로 바꿔야 할듯
                .logout((logout) ->
                        logout
//                                .logoutUrl("/logout")
//                                .logoutSuccessUrl("/login")
//                                .invalidateHttpSession(true)
//                                .deleteCookies("refresh-token", "JSESSIONID")
//                                .clearAuthentication(true))
                                .disable())
                                .build();
    }
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws
            Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

}
