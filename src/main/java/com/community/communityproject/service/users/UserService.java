package com.community.communityproject.service.users;

import com.amazonaws.services.s3.AmazonS3Client;
import com.community.communityproject.config.AmazonS3ResourceStorage;
import com.community.communityproject.dto.TokenDTO;
import com.community.communityproject.dto.users.UsersEditDTO;
import com.community.communityproject.dto.users.UsersInfo;
import com.community.communityproject.dto.users.UsersSignupDTO;
import com.community.communityproject.entity.users.ProfileImage;
import com.community.communityproject.entity.users.UserRole;
import com.community.communityproject.entity.users.Users;
import com.community.communityproject.repository.ProfileImageRepository;
import com.community.communityproject.repository.UserRepository;
import com.community.communityproject.service.jwt.AuthService;
import com.community.communityproject.service.jwt.TokenProvider;
import com.community.communityproject.service.redis.RedisService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.file.Paths;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    private final ProfileImageRepository profileImageRepository;

    private final PasswordEncoder passwordEncoder;

    private final AuthService authService;

    private final UserSecurityService userSecurityService;

    private final TokenProvider tokenProvider;

    private final RedisService redisService;

    private final AmazonS3ResourceStorage amazonS3ResourceStorage;

    private final AmazonS3Client amazonS3Client;


    private int RTCOOKIE_EXPIRATION;
    private int ATCOOKIE_EXPIRATION;

    private final String SERVER = "Server";

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    private final String PROFILE_PATH = "profileImage/userImg/";

    @Value("${app.upload.dir}")
    private String uploadDir;

    @Autowired
    public void setCookieExpiration(@Value("${jwt.refresh-token-validity-in-seconds}") int cookieExpiration) {
        this.RTCOOKIE_EXPIRATION = cookieExpiration;
    }

    @Autowired
    public void setAtCookieExpiration(@Value("${jwt.access-token-validity-in-seconds}") int atCookieExpiration) {
        this.ATCOOKIE_EXPIRATION = atCookieExpiration;
    }

    @Transactional
    public void signup(UsersSignupDTO usersSignupDTO) {

        Users users = new Users();
        users.setUsername(usersSignupDTO.getUsername());
        users.setEmail(usersSignupDTO.getEmail());
        users.setPassword(passwordEncoder.encode(usersSignupDTO.getPassword1()));
        users.setUserRole(UserRole.USER);
        MultipartFile multipartFile = usersSignupDTO.getProfileImage();
        String originName = multipartFile.getOriginalFilename();
        String filePath;
        long fileSize = multipartFile.getSize();

        // 기본 프로필
        if(originName.isEmpty() || originName.equals("profile_default.jpg")) {
            originName = "profile_default.jpg";
            filePath = String.valueOf(amazonS3Client.getUrl(bucket, "/image/profileImage/default/profile_default.jpg"));
            fileSize = 8636L;
        } else {
            // s3에 파일 저장
            filePath = amazonS3ResourceStorage.store(PROFILE_PATH, multipartFile);
            log.info("==========================");
            log.info("==========================");
            log.info("==========================");
            log.info(filePath);
            log.info("==========================");
            log.info("==========================");
            log.info("==========================");
        }
        log.info("========================");
        log.info("========================");
        log.info("FILE SIZE = " + fileSize);
        log.info("========================");
        log.info("========================");
        ProfileImage profileImage = ProfileImage.builder()
                        .originName(originName)
                        .filePath(filePath)
                        .fileSize(fileSize)
                        .users(users)
                        .build();

        userRepository.save(users);
        profileImageRepository.save(profileImage);
    }

    public UsersInfo loadUser(String email) {
        Users users = userRepository.findByEmail(email).orElseThrow();
        UsersInfo usersInfo = UsersInfo.builder()
                .username(users.getUsername())
                .email(users.getEmail())
                .userRole(users.getUserRole())
                .build();
        return usersInfo;
    }

    public UsersEditDTO loadEditUserInfo(String email) {
        Users users = userRepository.findByEmail(email).orElseThrow();
        UsersEditDTO usersEditDTO = new UsersEditDTO();
        usersEditDTO.setEmail(users.getEmail());
        usersEditDTO.setUsername(users.getUsername());
        return usersEditDTO;
    }

    /**
     * 프로필에서 게시글이나 댓글 불러올 때 유저 정보도 넣을라고 만듦
     * db안건드리고 UsersEditDTO 객체 생성
     * @param email
     * @param username
     * @return usersEditDTO
     */
    public UsersEditDTO getUsersInfo(String email, String username) {
        UsersEditDTO usersEditDTO = new UsersEditDTO();
        usersEditDTO.setEmail(email);
        usersEditDTO.setUsername(username);
        return usersEditDTO;
    }

    public String editUserCheck(UsersEditDTO usersEditDTO, String beforeEmail) {
        Users users = userRepository.findByEmail(beforeEmail).orElseThrow();
        // 기존 이메일이 아닌데 바꿀려는 이메일이 이미 존재하면
        if (!users.getEmail().equals(usersEditDTO.getEmail()) && userRepository.existsByEmail(usersEditDTO.getEmail())) {
            return "emailError";
        }
        // 기존 유저네임이 아닌데 바꿀려는 유저네임이 이미 존재하면
        if (!users.getUsername().equals(usersEditDTO.getUsername()) && userRepository.existsByUsername(usersEditDTO.getUsername())) {
            return "usernameError";
        }

        return "ok";
    }

    @Transactional
    public void editUser(UsersEditDTO usersEditDTO, String beforeEmail, HttpServletRequest request,
                         HttpServletResponse response, HttpSession session) {


        log.info("PROFILE NAME CHECK : " + usersEditDTO.getProfileImage().getOriginalFilename());
        // 토큰 검증
        TokenDTO tokenDTO = authService.validateToken(response, request);
        if (!tokenDTO.isEmpty()) {
            Users users = userRepository.findByEmail(beforeEmail).orElseThrow();
            users.setEmail(usersEditDTO.getEmail());
            users.setUsername(usersEditDTO.getUsername());
            users.setPassword(passwordEncoder.encode(usersEditDTO.getPassword()));
            users.setUserRole(UserRole.USER);

            users = userRepository.save(users);
            session.setAttribute("username", users.getUsername());
            session.setAttribute("email", users.getEmail());

            MultipartFile multipartFile = usersEditDTO.getProfileImage();
            String filePath = editProfileImage(multipartFile, usersEditDTO.getEmail());
            log.info("EDIT PROFILE IMAGE SUCCESS FILE PATH : " + filePath);
            // 토큰 재발행
            tokenDTO = authService.reissue(tokenDTO.getAccessToken(),
                    tokenDTO.getRefreshToken());
            // 토큰 재발급 후 쿠키랑 헤더에 저장
            authService.setCookie(response, tokenDTO.getAccessToken(), tokenDTO.getRefreshToken());
        }
    }

    @Transactional
    public String deleteUsers(HttpServletRequest request, HttpServletResponse response,
                              HttpSession session) {
        Cookie[] cookies = request.getCookies();
        String rt = null;
        String at = null;
        for (Cookie c : cookies) {
            if (c.getName().equals("refresh-token"))
                rt = c.getValue();
            else if (c.getName().equals("access-token")) {
                at = c.getValue();
            }
        }
        String email = null;

        // authService.validate가 true를 반환하면 at 만료일자 초과로 재발급
        if (authService.validate(at)) {
            TokenDTO tokenDTO = authService.reissue(at, rt);
            // tokenDTO가 null이면 rt가 없거나 탈취가능성 있음
            if (tokenDTO == null) {
                authService.logout(at, "logout");
                return null;
            }
        } else {
            email = authService.getPrincipal(at);
        }

        String refreshTokenInRedis = redisService.getValues("RT(" + SERVER + "):" + email);
        if (refreshTokenInRedis == null) { // Redis에 저장되어 있는 RT가 없을 경우
            return null; // -> 재로그인 요청
        }
        // 요청된 RT의 유효성 검사 & Redis에 저장되어 있는 RT와 같은지 비교
        if(!tokenProvider.validateRefreshToken(rt) || !refreshTokenInRedis.equals(rt)) {
            redisService.deleteValues("RT(" + SERVER + "):" + email); // 탈취 가능성 -> 삭제
            return null; // -> 재로그인 요청
        }
        Users users = userRepository.findByEmail(email).orElseThrow();
        // access-token 쿠키 삭제
        Cookie accessTokenCookie = new Cookie("access-token", "");
        accessTokenCookie.setMaxAge(0);
        accessTokenCookie.setHttpOnly(true);
        accessTokenCookie.setSecure(true);
        accessTokenCookie.setPath("/");
        response.addCookie(accessTokenCookie);

        // refresh-token 쿠키 삭제
        Cookie refreshTokenCookie = new Cookie("refresh-token", "");
        refreshTokenCookie.setMaxAge(0);
        refreshTokenCookie.setHttpOnly(true);
        refreshTokenCookie.setSecure(true);
        refreshTokenCookie.setPath("/");
        response.addCookie(refreshTokenCookie);

        session.removeAttribute("username");
        session.removeAttribute("email");
        session.removeAttribute("viewedBoards");

        ProfileImage profileImage = profileImageRepository.findByUsers(users);
        String path = profileImage.getFilePath();
        String filename = path.substring(path.indexOf("/") + 1);

        authService.logout(at, "delete");
        userRepository.delete(users);
        // 프로필 사진 삭제
        amazonS3Client.deleteObject(bucket, filename);

        return "ok";
    }

    /**
     * 프로필 이미지 수정 할 때 씀
     * @param email
     * @return profileImage
     */
    public ProfileImage getProfileImage(String email) {
        Users users = userRepository.findByEmail(email).orElseThrow();
        return profileImageRepository.findByUsers(users);
    }

    /**
     * 프로필 이미지 수정 할 때 씀
     * @param multipartFile
     * @param email
     * @return filePath
     */
    private String editProfileImage(MultipartFile multipartFile, String email) {
        String originName = multipartFile.getOriginalFilename();
        String filePath = String.valueOf(amazonS3Client.getUrl(bucket, "/image/profileImage/default/profile_default.jpg"));
        long fileSize = multipartFile.getSize();
        ProfileImage profileImage = getProfileImage(email);
        // 현재 filePath 가져오기
        String path = profileImage.getFilePath();
        // 맨 첫번째 "/" 이후의 문자열
        String filename = path.substring(path.indexOf("/") + 1);
        if(originName.equals("profile_default.jpg") || originName.isEmpty()) {
            // 만약 디폴트 이미지 말고 다른 이미지가 존재하면
            if (amazonS3Client.doesObjectExist(bucket, filename)) {
                // s3에서 기존 이미지 삭제
                amazonS3Client.deleteObject(bucket, filename);
                profileImage.setOriginName("profile_default.jpg");
                profileImage.setFilePath("profileImage\\default\\profile_default.jpg");
                profileImage.setFileSize(8636L);
                // db에 기본 이미지로 저장
                profileImageRepository.save(profileImage);
            }
            // 이미 기본 이미지거나 프로필 사진을 취소했으면 그냥 경로 리턴
            return filePath;
        } else {
            // 일단 저장
            filePath = amazonS3ResourceStorage.store(PROFILE_PATH, multipartFile);
            // 저장했으면 이전 이미지 삭제
            amazonS3Client.deleteObject(bucket, filename);
        }

        // 프로필 이미지 수정
        profileImage.setOriginName(originName);
        profileImage.setFilePath(filePath);
        profileImage.setFileSize(fileSize);
        profileImageRepository.save(profileImage);

        // 수정된 경로 리턴
        return filePath;
    }

    /**
     * 회원가입 및 프로필 이미지 수정 용도
     * @param multipartFile
     * @return file path
     */
//    private String saveProfileImage(MultipartFile multipartFile) {
//        try {
//            Path dir = Paths.get(uploadDir);
//            log.info("UPLOADDIR : " + dir.toAbsolutePath());
//            if (!Files.exists(dir)) {
//                Files.createDirectories(dir);
//            }
//
//            // 파일의 확장자 추출
//            String originalFileExtension;
//            String contentType = multipartFile.getContentType();
//
//            // 확장자명이 존재하지 않을 경우 처리 x
//            if (ObjectUtils.isEmpty(contentType)) {
//                log.error("The extension name does not exist.");
//                log.error("File Name : " + multipartFile.getOriginalFilename());
//                throw new RuntimeException("Invalid content type");
//            } else {
//                if (contentType. contains("image/jpeg"))
//                    originalFileExtension = ".jpg";
//                else if (contentType. contains("image/png"))
//                    originalFileExtension = ".png";
//                else {
//                    log.error("Only extensions of jpg and png are allowed.");
//                    log.error("File Name : " + multipartFile.getOriginalFilename());
//                    throw new RuntimeException("Invalid content type");
//                }
//            }
//
//            String filename = "profileImage_" + System.currentTimeMillis() + "_" + multipartFile.getOriginalFilename();
//
//            // Build the file path
//            Path filePath = dir.resolve(filename);
//
//            try {
//                log.info("FILE PATH : " + filePath);
//                // Write the file to the filesystem
//                // 저장할 땐 절대경로
//                Files.copy(multipartFile.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
//                log.info("File exists? : " + Files.exists(filePath));
//            } catch (IOException e) {
//                throw new RuntimeException("Could not store file : " + multipartFile.getOriginalFilename());
//            }
//
//
//            // Return the file path
//            // 리턴값은 절대경로 주면 안됨
//            return Paths.get("profileImage", "userImg", filename).toString();
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }
//    }

    private String saveProfileImage(MultipartFile multipartFile) {
        File dir = new File(uploadDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        String originalFileExtension;
        String contentType = multipartFile.getContentType();

        if (ObjectUtils.isEmpty(contentType)) {
            log.error("The extension name does not exist.");
            log.error("File Name : " + multipartFile.getOriginalFilename());
            throw new RuntimeException("Invalid content type");
        } else {
            if (contentType.contains("image/jpeg"))
                originalFileExtension = ".jpg";
            else if (contentType.contains("image/png"))
                originalFileExtension = ".png";
            else {
                log.error("Only extensions of jpg and png are allowed.");
                log.error("File Name : " + multipartFile.getOriginalFilename());
                throw new RuntimeException("Invalid content type");
            }
        }

        String filename = "profileImage_" + System.currentTimeMillis() + "_" + multipartFile.getOriginalFilename();

        File file = new File(dir + File.separator + filename);

        try {
            log.info("File PATH : " + file.getAbsolutePath());
            multipartFile.transferTo(file);
            log.info("File exists? : " + file.exists());
        } catch (IOException e) {
            log.error("ERROR : " + e);
            throw new RuntimeException("Could not store file : " + multipartFile.getOriginalFilename());
        }

        return Paths.get("profileImage", "userImg", filename).toString();
    }

    /**
     * 프로필 이미지 가져올 때 사용
     * 기본 프로필이면 로컬에 있는 이미지
     * 아니면 s3에 있는 이미지 가져옴
     * @param email
     * @return profileImage.getFilePath() or amazonS3Client.getUrl().toString()
     */
    public String findImage(String email) {
        Users users = userRepository.findByEmail(email).orElseThrow();

        ProfileImage profileImage = profileImageRepository.findByUsers(users);
        // 기본 프로필이면 로컬에 있는 디폴트 프로필 url 반환
        if (profileImage.getOriginName().equals("profile_default.jpg")) {
            return profileImage.getFilePath();
        }
        String path = profileImage.getFilePath();

        path = trimUrlToPath(path);

        // 맨 첫번째 "/" 이후의 문자열
        // String filename = path.substring(path.indexOf("/") + 1);
        // s3에 있는 이미지 가져옴
        return amazonS3Client.getUrl(bucket, path).toString();
    }

    /**
     * filePath를 bucket url을 짤라서 반환함
     * @param fullUrl
     * @return path
     */
    public String trimUrlToPath(String fullUrl) {
        int imageIndex = fullUrl.indexOf("/image");

        if(imageIndex != -1) {
            return fullUrl.substring(imageIndex + 1);
        }
        return fullUrl; // "/image"가 없는 경우 원래 URL 반환
    }

}
