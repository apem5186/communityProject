package com.community.communityproject.config;

import org.springframework.util.StringUtils;

import java.util.UUID;

/**
 * 출처 : https://antdev.tistory.com/93
 */
public class MultipartUtil {
    private static final String BASE_DIR ="images";


    /**
     * 로컬에서의 사용자 홈 디렉토리 경로를 반환합니다.
     */
    public static String getLocalHomeDirectory() {
        return System.getProperty("user.home");
    }

    /**
     * 새로운 파일 고유 ID를 생성합니다.
     * @return 36자리의 UUID
     */
    public static String createFileId() {
        return UUID.randomUUID().toString();
    }

    /**
     * Multipart 의 ContentType 값에서 / 이후 확장자만 잘라냅니다.
     * @param contentType ex) image/png
     * @return ex) png
     */
    public static String getFormat(String contentType) {
        if (StringUtils.hasText(contentType)) {
            return contentType.substring(contentType.lastIndexOf('/') + 1);
        }
        return null;
    }

    /**
     * 파일의 전체 경로를 생성합니다.
     * @param restPath BASE_DIR = "image", restPath = 나머지 경로
     * @param fileName 생성된 파일 고유 ID
     */
    public static String createPath(String restPath, String fileName) {
        return String.format("%s/%s/%s", BASE_DIR, restPath, fileName);
    }
}
