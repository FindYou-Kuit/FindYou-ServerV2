package com.kuit.findyou.global.infrastructure;

import org.springframework.web.multipart.MultipartFile;

public interface ImageUploader {
    String upload(MultipartFile file) throws FileUploadingFailedException;
    void delete(String s3ObjectKey);
}
