package com.kuit.findyou.domain.image.service;

import com.kuit.findyou.global.common.exception.CustomException;
import com.kuit.findyou.global.infrastructure.FileUploadingFailedException;
import com.kuit.findyou.global.infrastructure.ImageUploader;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

import static com.kuit.findyou.global.common.response.status.BaseExceptionResponseStatus.*;

@Service
@RequiredArgsConstructor
public class ReportImageUploadServiceImpl implements ReportImageUploadService{
    private final ImageUploader imageUploader;

    @Override
    public List<String> uploadImages(List<MultipartFile> files) {
        final int MAX_FILES = 5; //이미지는 최대 5장

        if (files == null || files.isEmpty()) return List.of(); //빈 배요소여도 에러 X. 그냥 빈 배열 응답
        if (files.size() > MAX_FILES) throw new CustomException(IMAGE_UPLOAD_LIMIT_EXCEEDED);

        List<String> urls = new ArrayList<>();
        for (MultipartFile file : files) {
            if (file == null || file.isEmpty()) continue; //빈 값이 들어오더라도 허용

            //간단한 MIME 체크
            String ct = file.getContentType();
            if (ct != null) {
                String lower = ct.toLowerCase();
                if (!lower.startsWith("image/") && !lower.equals("application/octet-stream") && !lower.equals("binary/octet-stream")) {
                    throw new CustomException(INVALID_IMAGE_FORMAT);
                }
            }
            try {
                //업로더가 S3에 업로드 & CDN url로 반환
                String cdnUrl = imageUploader.upload(file);
                urls.add(cdnUrl);
            } catch (FileUploadingFailedException e) {
                throw new CustomException(IMAGE_UPLOAD_FAILED);
            }
        }
        return urls;
    }
}
