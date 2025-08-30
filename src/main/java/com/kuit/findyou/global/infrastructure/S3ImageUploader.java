package com.kuit.findyou.global.infrastructure;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

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

    private String getFileUrl(String fileName) {
        return s3baseUrl + "/" + fileName;
    }
}
