package com.kuit.findyou.domain.report.service.retrieve;

import com.kuit.findyou.domain.image.model.ReportImage;
import com.kuit.findyou.domain.report.dto.response.MissingReportDetailResponseDTO;
import com.kuit.findyou.domain.report.model.MissingReport;
import com.kuit.findyou.domain.report.repository.MissingReportRepository;
import com.kuit.findyou.domain.report.service.detail.strategy.MissingReportDetailStrategy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@RequiredArgsConstructor
@Service
@Slf4j
public class MissingReportRetrieveWithS3ServiceImpl implements MissingReportRetrieveWithS3Service{
    private final MissingReportRepository missingReportRepository;
    private final MissingReportDetailStrategy missingReportDetailStrategy;

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

            List<String> imageUrls = report.getReportImages().stream()
                    .map(ReportImage::getImageUrl)
                    .filter(url -> url != null && !url.isBlank())
                    .distinct()
                    .toList();

            MissingReportDetailResponseDTO dto =
                    missingReportDetailStrategy.toDetailDto(report, imageUrls,false);
            result.add(dto);
        }

        return result;
    }
}
