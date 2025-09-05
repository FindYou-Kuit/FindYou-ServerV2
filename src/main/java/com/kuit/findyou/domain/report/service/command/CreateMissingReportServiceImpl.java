package com.kuit.findyou.domain.report.service.command;

import com.kuit.findyou.domain.image.model.ReportImage;
import com.kuit.findyou.domain.image.repository.ReportImageRepository;
import com.kuit.findyou.domain.report.dto.request.CreateMissingReportRequest;
import com.kuit.findyou.domain.report.model.MissingReport;
import com.kuit.findyou.domain.report.model.Report;
import com.kuit.findyou.domain.report.model.ReportTag;
import com.kuit.findyou.domain.report.model.Sex;
import com.kuit.findyou.domain.report.repository.MissingReportRepository;
import com.kuit.findyou.domain.user.model.User;
import com.kuit.findyou.domain.user.repository.UserRepository;
import com.kuit.findyou.global.common.exception.CustomException;
import com.kuit.findyou.global.external.client.KakaoCoordinateClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.stream.Collectors;

import static com.kuit.findyou.global.common.response.status.BaseExceptionResponseStatus.BAD_REQUEST;

@Service
@RequiredArgsConstructor
public class CreateMissingReportServiceImpl implements CreateMissingReportService {
    private final MissingReportRepository missingReportRepository;
    private final ReportImageRepository reportImageRepository;
    private final UserRepository userRepository;
    private final KakaoCoordinateClient kakaoCoordinateClient;


    private static final DateTimeFormatter DOT_DATE = DateTimeFormatter.ofPattern("yyyy.MM.dd");

    @Transactional
    @Override
    public void createMissingReport(CreateMissingReportRequest req, Long userId) {
        User user = userRepository.getReferenceById(userId);
        MissingReport report = createMissingReportFromRequest(req, user);
        missingReportRepository.save(report); //신고글 저장
        saveReportImages(req.imgUrls(), report);//이미지 entity 생성 및 연결
    }


    private MissingReport createMissingReportFromRequest(CreateMissingReportRequest req, User user) {
        // 필수값 검증
        requireText(req.species());
        requireText(req.breed());
        requireText(req.sex());
        requireText(req.age());
        requireText(req.missingDate());
        requireText(req.location());
        requireText(req.landmark());
        requireText(req.furColor());

        KakaoCoordinateClient.Coordinate coordinate = kakaoCoordinateClient.requestCoordinateOrDefault(req.location());

        // 형식 변환
        LocalDate date = parseDotDate(req.missingDate());
        Sex sex = mapSexStrict(req.sex());
        BigDecimal lat = coordinate.latitude();
        BigDecimal lng = coordinate.longitude();

        return MissingReport.createMissingReport(
                req.breed(), req.species(), ReportTag.MISSING, date,
                req.location(), user,
                sex, req.rfid(), req.age(),
                req.furColor(), req.significant(),
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

    //헬퍼 메서드들
    private void requireText(String v) {
        if (v == null || v.trim().isEmpty()) throw new CustomException(BAD_REQUEST);
    }

    private LocalDate parseDotDate(String date) {
        try { return LocalDate.parse(date, DOT_DATE); }
        catch (DateTimeParseException e) { throw new CustomException(BAD_REQUEST); }
    }
    private Sex mapSexStrict(String input) {
        String s = input.trim();
        if (s.equals("남자")) return Sex.M;
        if (s.equals("여자")) return Sex.F;
        throw new CustomException(BAD_REQUEST);
    }
}
