package com.kuit.findyou.domain.report.service.retrieve;

import com.kuit.findyou.domain.image.model.ReportImage;
import com.kuit.findyou.domain.report.dto.response.MissingReportDetailResponseDTO;
import com.kuit.findyou.domain.report.model.MissingReport;
import com.kuit.findyou.domain.report.repository.MissingReportRepository;
import com.kuit.findyou.domain.report.service.detail.strategy.MissingReportDetailStrategy;
import com.kuit.findyou.global.infrastructure.FileUploadingFailedException;
import com.kuit.findyou.global.infrastructure.ImageUploader;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@Service
@Slf4j
public class MissingReportRetrieveWithS3ServiceImpl implements MissingReportRetrieveWithS3Service{
    private final MissingReportRepository missingReportRepository;
    private final ImageUploader imageUploader;
    private final MissingReportDetailStrategy missingReportDetailStrategy;

    //외부 URL에서 이미지를 받아오기 위한 HTTP 클라이언트
    private final RestTemplate restTemplate;

    @Override
    @Transactional(readOnly = true)
    public List<MissingReportDetailResponseDTO> getRandomMissingReportsWithS3(int count) {
        LocalDate yesterday = LocalDate.now().minusDays(1);

        List<MissingReport> allReports = missingReportRepository.findByDate(yesterday);

        if (allReports.isEmpty()) {
            // 204 no content
            return Collections.emptyList();
        }

        Collections.shuffle(allReports);

        int limit = Math.max(1, count);
        List<MissingReport> selectedReports = allReports.stream().limit(limit).toList();

        List<MissingReportDetailResponseDTO> result = new ArrayList<>();

        for (MissingReport report : selectedReports) {

            // 실종글에 연결된 원본 이미지 가져오기
            List<ReportImage> reportImages = report.getReportImages();
            List<String> originalUrls = reportImages.stream()
                    .map(ReportImage::getImageUrl)
                    .toList();

            // S3에 업로드하기
            List<String> s3Urls = new ArrayList<>();
            for (String url : originalUrls) {
                try {
                    byte[] imageBytes = restTemplate.getForObject(url, byte[].class);
                    if (imageBytes == null || imageBytes.length == 0) {
                        log.warn("[MissingReportS3] 빈 이미지 응답: url={}", url);
                        continue;
                    }

                    String key = "missing/" + report.getId() + "/" + UUID.randomUUID() + ".jpg";
                    String s3Url = imageUploader.upload(imageBytes, key, "image/jpeg");
                    s3Urls.add(s3Url);

                } catch (FileUploadingFailedException e) {
                    // S3 자체 장애, 권한 등의 문제 발생 시
                    log.error("[MissingReportS3] S3 업로드 실패 - reportId={}, url={}, message={}",
                            report.getId(), url, e.getMessage(), e);
                } catch (Exception e) {
                    log.error("[MissingReportS3] 이미지 처리 중 예기치 못한 오류 발생 - reportId={}, url={}",
                            report.getId(), url, e);
                }
            }

            MissingReportDetailResponseDTO dto =
                    missingReportDetailStrategy.toDetailDto(report, s3Urls,false);
            result.add(dto);
        }

        return result;
    }
}
