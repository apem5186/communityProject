package com.community.communityproject.config;

import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.PutObjectRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

import static com.community.communityproject.config.MultipartUtil.createPath;

@Component
@Slf4j
@RequiredArgsConstructor
public class AmazonS3ResourceStorage {

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    private final AmazonS3Client amazonS3Client;

    public String store(String fullPath, MultipartFile multipartFile) {
        String sp = File.separator;
        File file = new File(MultipartUtil.getLocalHomeDirectory(), fullPath);
        String contentType = multipartFile.getContentType();
        String filename;
        int index = fullPath.indexOf("/");
        try {
            // 무슨 용도의 이미지인지 검사 현재는 profileImage만 있음
            if (index != -1) {
                // "/" 나오기 전까지 짤라냄
                String result = fullPath.substring(0, index);
                // 프로필 이미지일 경우
                if (result.equals("profileImage")) {
                    // s3에 한글 파일명 깨져서 UUID 돌림
                    filename = "profileImage_" + System.currentTimeMillis() +
                            "_" + UUID.randomUUID();
                    fullPath = "image" + "/" + fullPath + filename;
                    file = new File(MultipartUtil.getLocalHomeDirectory(), fullPath);
                } else if (result.equals("boardImage")) {
                    // 경로에서 category 추출
                    String[] parts = fullPath.split("/");
                    String category = parts[1];
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                    String today = sdf.format(new Date()) + "/";

                    Path folderPath = Path.of(createPath(fullPath, today));

                    log.info("FOLDER PATH : " + folderPath);
                    // 폴더 없으면 생성
                    if (!Files.exists(Path.of(
                            MultipartUtil.getLocalHomeDirectory() +"/"+ folderPath))) {
                        Files.createDirectories(Path.of(
                                MultipartUtil.getLocalHomeDirectory() +"/"+ folderPath));
                        log.info("create folder");
                    }
                    // s3에 한글 파일명 잘 안올라가서 그냥 랜덤 돌림
                    filename = "boardImage_" + System.currentTimeMillis() +
                            "_" + UUID.randomUUID();
                    String path = folderPath.toString().replace('\\', '/');
                    fullPath = path + "/" + filename;
                    file = new File(MultipartUtil.getLocalHomeDirectory(), fullPath);
                } else {
                    throw new RuntimeException("올바르지 않은 경로입니다.");
                }
            } else {
                throw new RuntimeException("경로에 /이 포함되어 있지 않습니다.");
            }

            // 확장자 관련 검사
            if (ObjectUtils.isEmpty(contentType)) {
                // 확장자가 없을 때
                log.error("The extension name does not exist.");
                log.error("File Name : " + multipartFile.getOriginalFilename());
                throw new RuntimeException("Invalid content type");
            } else {
                // jpeg(jpg), png 아니면 안 됨
                if (!contentType.contains("image/jpeg") && !contentType.contains("image/png")) {
                    log.error("Only extensions of jpg and png are allowed.");
                    log.error("File Name : " + multipartFile.getOriginalFilename());
                    throw new RuntimeException("Invalid content type");
                }

            }
            // 로컬에 일단 저장
            multipartFile.transferTo(file);
            // s3에 저장
            amazonS3Client.putObject(new PutObjectRequest(bucket, fullPath, file)
                    .withCannedAcl(CannedAccessControlList.PublicRead));
            log.info("aws s3 file path : " + amazonS3Client.getUrl(bucket, fullPath));
            return amazonS3Client.getUrl(bucket, fullPath).toString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            // 로컬에 있다면 로컬에 있는거 삭제
            if (file.exists()) {
                file.delete();
            }
        }
    }
}
