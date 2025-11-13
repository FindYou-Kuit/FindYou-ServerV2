package com.kuit.findyou.global.infrastructure;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class S3ImageUploader implements ImageUploader {
    private final S3Client s3Client;

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    @Value("${cloud.aws.s3.base-url}")
    private String s3baseUrl;

    @Override
    public String upload(MultipartFile file) throws FileUploadingFailedException{
        if(file.isEmpty() || file == null){
            throw new IllegalArgumentException("업로드할 파일이 비어 있을 수 없습니다");
        }

        // 날짜와 uuid로 고유한 이름 생성
        String originalName = file.getOriginalFilename();
        String datePath = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
        String fileName = datePath + "/" + UUID.randomUUID() + "_" + originalName;

        try{
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucket)
                    .key(fileName)
                    .contentType(file.getContentType())
                    .build();

            s3Client.putObject(putObjectRequest,
                    RequestBody.fromInputStream(file.getInputStream(), file.getSize()));

            return getFileUrl(fileName);

        } catch (IOException e) {
            throw new FileUploadingFailedException("파일 변환 중 오류 발생");
        } catch (S3Exception e) {
            throw new FileUploadingFailedException("S3 업로드 실패: " + e.awsErrorDetails().errorMessage());
        }
    }

    @Override
    public void delete(String s3ObjectKey) {
        if (s3ObjectKey == null || s3ObjectKey.isBlank()) {
            throw new IllegalArgumentException("S3에서 삭제할 객체 키가 없습니다.");
        }
        try {
            DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder() //삭제 요청 객체
                    .bucket(bucket)
                    .key(s3ObjectKey)
                    .build();

            s3Client.deleteObject(deleteObjectRequest); //삭제 요청
            log.info("S3에서 이미지 삭제 성공: key={}", s3ObjectKey);

        } catch (S3Exception e) {
            //S3 이미지 삭제에 실패 시, 전체 로직이 멈추면 안되므로 로그만
            log.error("S3 이미지 삭제 실패: key={}, error={}", s3ObjectKey, e.awsErrorDetails().errorMessage(), e);
        }
    }


    private String getFileUrl(String fileName) {
        return s3baseUrl + "/" + fileName;
    }
}
