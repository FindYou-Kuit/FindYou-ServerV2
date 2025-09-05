package com.kuit.findyou.domain.report.service.command;

import com.kuit.findyou.domain.image.model.ReportImage;
import com.kuit.findyou.domain.report.model.Report;
import com.kuit.findyou.domain.report.repository.ReportRepository;
import com.kuit.findyou.global.common.exception.CustomException;
import com.kuit.findyou.global.infrastructure.ImageUploader;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

import static com.kuit.findyou.global.common.response.status.BaseExceptionResponseStatus.*;

@RequiredArgsConstructor
@Service
public class DeleteReportServiceImpl implements DeleteReportService {

    private final ReportRepository reportRepository;
    private final ImageUploader imageUploader;

    @Override
    @Transactional
    public void deleteReport(Long reportId, Long userId) {
        Report report = reportRepository.findByIdWithUserAndImages(reportId)
                .orElseThrow(() -> new CustomException(REPORT_NOT_FOUND));

        if(!report.getUser().getId().equals(userId)){
            throw new CustomException(MISMATCH_REPORT_USER);
        }
        List<ReportImage> imagesToDelete = report.getReportImages();

        //이미지 존재하면 S3에서 삭제
        if (imagesToDelete != null && !imagesToDelete.isEmpty()) {
            imagesToDelete.stream()
                    .map(ReportImage::getImageUrl)
                    .forEach(imageUrl -> {
                        String imageKey = extractImageKeyFromUrl(imageUrl);
                        imageUploader.delete(imageKey);
                    });
        }
        reportRepository.delete(report);
    }
    private String extractImageKeyFromUrl(String url) {
        try {
            return new URI(url).getPath().substring(1);
        } catch (URISyntaxException e) {
            throw new CustomException(BAD_REQUEST);
        }
    }
}
