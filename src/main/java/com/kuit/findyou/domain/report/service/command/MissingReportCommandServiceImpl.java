package com.kuit.findyou.domain.report.service.command;

import com.kuit.findyou.domain.image.model.ReportImage;
import com.kuit.findyou.domain.image.repository.ReportImageRepository;
import com.kuit.findyou.domain.report.dto.request.CreateMissingReportRequest;
import com.kuit.findyou.domain.report.model.MissingReport;
import com.kuit.findyou.domain.report.model.ReportTag;
import com.kuit.findyou.domain.report.model.Sex;
import com.kuit.findyou.domain.report.repository.MissingReportRepository;
import com.kuit.findyou.domain.user.model.User;
import com.kuit.findyou.domain.user.repository.UserRepository;
import com.kuit.findyou.global.common.exception.CustomException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URI;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import static com.kuit.findyou.global.common.response.status.BaseExceptionResponseStatus.BAD_REQUEST;
import static com.kuit.findyou.global.common.response.status.BaseExceptionResponseStatus.IMAGE_UPLOAD_HTTPS_REQUIRED;

@Service
@RequiredArgsConstructor
public class MissingReportCommandServiceImpl implements MissingReportCommandService {
    private final MissingReportRepository missingReportRepository;
    private final ReportImageRepository reportImageRepository;
    private final UserRepository userRepository;

    @Value("${image.public-base-url:}")
    private String cdnBase;

    private static final DateTimeFormatter DOT_DATE = DateTimeFormatter.ofPattern("yyyy.MM.dd");

    @Transactional
    @Override
    public void createMissingReport(CreateMissingReportRequest req, Long userId) {
        try {
            if (userId == null) throw new CustomException(BAD_REQUEST);
            User user = userRepository.getReferenceById(userId);
            // 필수값 검증
            requireText(req.species());
            requireText(req.breed());
            requireText(req.sex());
            requireText(req.age());
            requireText(req.missingDate());
            requireText(req.location());
            requireText(req.landmark());
            requireText(req.furColor());
            requireNotNull(req.latitude());
            requireNotNull(req.longitude());

            //형식 변환
            LocalDate date = parseDotDate(req.missingDate());
            Sex sex = mapSexStrict(req.sex());
            BigDecimal lat = toScale(req.latitude());
            BigDecimal lng = toScale(req.longitude());

            //엔티티 저장
            MissingReport saved = missingReportRepository.save(
                    MissingReport.createMissingReport(
                            req.breed(), req.species(), ReportTag.MISSING, date,
                            req.location(), user,
                            sex, req.rfid(), req.age(),
                            req.furColor(), req.significant(),
                            req.landmark(), lat, lng
                    )
            );

            //이미지 저장 (CDN URL만 허용)
            Optional.ofNullable(req.imgUrls())
                    .orElseGet(List::of)
                    .stream()
                    .filter(Objects::nonNull)
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .peek(this::validateCdn)
                    .distinct()
                    .forEach(url -> {
                        ReportImage img = ReportImage.createReportImage(url, UUID.randomUUID().toString());
                        img.setReport(saved);
                        reportImageRepository.save(img);
                    });

        } catch (CustomException ce) {
            throw ce;
        } catch (Exception e) {
            throw new IllegalArgumentException("서버 내부 오류가 발생하였습니다.");
        }
    }
    //헬퍼 메서드 생성
    private void requireText(String v) {
        if (v == null || v.trim().isEmpty()) throw new CustomException(BAD_REQUEST);
    }
    private void requireNotNull(Object v) {
        if (v == null) throw new CustomException(BAD_REQUEST);
    }

    private LocalDate parseDotDate(String s) {
        try { return LocalDate.parse(s, DOT_DATE); }
        catch (DateTimeParseException e) { throw new CustomException(BAD_REQUEST); }
    }
    private BigDecimal toScale(Double v) {
        return v == null ? null : BigDecimal.valueOf(v).setScale(6, RoundingMode.HALF_UP);
    }
    private Sex mapSexStrict(String input) {
        String s = input.trim();
        if (s.equals("남자")) return Sex.M;
        if (s.equals("여자")) return Sex.F;
        throw new CustomException(BAD_REQUEST);
    }
    private void validateCdn(String url) {
        if (cdnBase == null || cdnBase.isBlank()) throw new IllegalArgumentException("CDN 기본 URL이 누락되었습니다.");
        try {
            var uri = URI.create(url.trim());

            // https 강제
            if (!"https".equalsIgnoreCase(uri.getScheme())) {
                throw new CustomException(IMAGE_UPLOAD_HTTPS_REQUIRED);
            }
        } catch (IllegalArgumentException e) {
            throw new CustomException(BAD_REQUEST);
        }
    }
}
