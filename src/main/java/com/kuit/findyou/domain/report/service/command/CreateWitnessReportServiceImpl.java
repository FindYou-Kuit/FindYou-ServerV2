package com.kuit.findyou.domain.report.service.command;

import com.kuit.findyou.domain.image.model.ReportImage;
import com.kuit.findyou.domain.image.repository.ReportImageRepository;
import com.kuit.findyou.domain.report.dto.request.CreateMissingReportRequest;
import com.kuit.findyou.domain.report.dto.request.CreateWitnessReportRequest;
import com.kuit.findyou.domain.report.model.*;
import com.kuit.findyou.domain.report.repository.WitnessReportRepository;
import com.kuit.findyou.domain.user.model.User;
import com.kuit.findyou.domain.user.repository.UserRepository;
import com.kuit.findyou.global.common.exception.CustomException;
import com.kuit.findyou.global.external.client.KakaoCoordinateClient;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.stream.Collectors;

import static com.kuit.findyou.global.common.response.status.BaseExceptionResponseStatus.BAD_REQUEST;

@Service
@RequiredArgsConstructor
public class CreateWitnessReportServiceImpl implements CreateWitnessReportService {

    private final WitnessReportRepository witnessReportRepository;
    private final ReportImageRepository reportImageRepository;
    private final UserRepository userRepository;
    private final KakaoCoordinateClient kakaoCoordinateClient;


    private static final DateTimeFormatter DOT_DATE = DateTimeFormatter.ofPattern("yyyy.MM.dd");

    @Transactional
    @Override
    public void createWitnessReport(CreateWitnessReportRequest req, Long userId) {
        User user = userRepository.getReferenceById(userId);
        WitnessReport report = createWitnessReportFromRequest(req, user);
        witnessReportRepository.save(report); // 목격글 저장
        saveReportImages(req.imgUrls(), report); // 이미지 저장
    }

    private WitnessReport createWitnessReportFromRequest(CreateWitnessReportRequest req, User user) {
        // 필수값 검증
        requireText(req.species());
        requireText(req.breed());
        requireText(req.foundDate());
        requireText(req.location());
        requireText(req.landmark());
        requireText(req.furColor());

        KakaoCoordinateClient.Coordinate coordinate = kakaoCoordinateClient.requestCoordinateOrDefault(req.location());

        // 형식 변환
        LocalDate date = parseDotDate(req.foundDate());
        BigDecimal lat = coordinate.latitude();
        BigDecimal lng = coordinate.longitude();

        return WitnessReport.createWitnessReport(
                req.breed(), req.species(), ReportTag.WITNESS, date,
                req.location(), user,
                req.furColor(), req.significant(), user.getName(),
                req.landmark(), lat, lng
        );
    }

    private void saveReportImages(List<String> imageUrls, Report savedReport) {
        if (imageUrls == null || imageUrls.isEmpty()) {
            return; // 이미지가 없으면 아무것도 하지 않음
        }

        List<ReportImage> images = imageUrls.stream()
                .filter(url -> url != null && !url.isBlank()) //null이나 빈 문자열 URL은 제외
                .distinct() //중복 URL 제거
                .map(url -> ReportImage.createReportImage(url, savedReport))
                .collect(Collectors.toList());

        reportImageRepository.saveAll(images);
    }

    // 헬퍼 메서드
    private void requireText(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new CustomException(BAD_REQUEST);
        }
    }

    private LocalDate parseDotDate(String date) {
        try {
            return LocalDate.parse(date, DOT_DATE);
        } catch (DateTimeParseException e) {
            throw new CustomException(BAD_REQUEST);
        }
    }
}
