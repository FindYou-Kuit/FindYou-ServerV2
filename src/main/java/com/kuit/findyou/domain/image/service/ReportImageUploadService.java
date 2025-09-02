package com.kuit.findyou.domain.image.service;

import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ReportImageUploadService {
    List<String> uploadImages(List<MultipartFile> files);
}
