package com.kuit.findyou.domain.user.service.interest_report;

import com.kuit.findyou.domain.report.dto.response.CardResponseDTO;
import com.kuit.findyou.domain.report.dto.response.ReportProjection;
import com.kuit.findyou.domain.report.factory.CardFactory;
import com.kuit.findyou.domain.report.model.InterestReport;
import com.kuit.findyou.domain.report.model.Report;
import com.kuit.findyou.domain.report.repository.InterestReportRepository;
import com.kuit.findyou.domain.report.repository.ReportRepository;
import com.kuit.findyou.domain.user.model.User;
import com.kuit.findyou.domain.user.repository.UserRepository;
import com.kuit.findyou.global.common.exception.CustomException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static com.kuit.findyou.global.common.response.status.BaseExceptionResponseStatus.*;

@Slf4j
@RequiredArgsConstructor
@Transactional
@Service
public class InterestReportServiceImpl implements InterestReportService{
    private final InterestReportRepository interestReportRepository;
    private final ReportRepository reportRepository;
    private final UserRepository userRepository;
    private final CardFactory cardFactory;

    @Override
    public CardResponseDTO retrieveInterestAnimals(Long userId, Long lastId, int size) {
        log.info("[retrieveInterestAnimals] userId = {}, lastId = {}, size = {}", userId, lastId, size);
        // 유저가 관심 게시글로 등록한 게시글을 최신순으로 페이징하여 조회
        List<ReportProjection> interestReportProjections = interestReportRepository.findInterestReportsByCursor(userId, lastId, PageRequest.of(0, size + 1));
        List<ReportProjection> takenWithSize = takeWithSize(size, interestReportProjections);
        Set<Long> interestIds = takenWithSize.stream().map(ReportProjection::getReportId).collect(Collectors.toSet());
        long nextLastId = getLastId(takenWithSize);

        return cardFactory.createCardResponse(
                takenWithSize,
                interestIds,
                nextLastId,
                interestReportProjections.size() <= size
            );
    }

    @Override
    public void addInterestAnimal(Long userId, Long reportId) {
        log.info("[addInterestAnimals] userId = {}, reportId = {}", userId, reportId);

        // 사용자 찾기
        User user = userRepository.getReferenceById(userId);

        // 리포트 찾기
        Report report = reportRepository.findById(reportId).orElseThrow(() -> new CustomException(REPORT_NOT_FOUND));

        // 이미 관심글로 등록되었으면 예외 발생
        if(interestReportRepository.existsByReportIdAndUserId(reportId, userId)){
            throw new CustomException(DUPLICATE_INTEREST_REPORT);
        }

        // 관심글로 등록
        InterestReport interestReport = InterestReport.createInterestReport(user, report);
        interestReportRepository.save(interestReport);
    }

    @Override
    public void deleteInterestAnimal(Long userId, Long reportId) {
        log.info("[deleteInterestAnimals] userId = {}, reportId = {}", userId, reportId);

        // 사용자 조회
        User user = userRepository.getReferenceById(userId);

        // 신고글 조회
        Optional<Report> reportById = reportRepository.findById(reportId);

        // 신고글이 없으면 중단
        if(reportById.isEmpty()){
            log.info("[deleteInterestAnimals] reportId = {}인 신고글이 존재하지 않음", reportId);
            return;
        }

        // 관심신고글을 삭제
        Report report = reportById.get();
        interestReportRepository.deleteByUserAndReport(user, report);

        log.info("[deleteInterestAnimals] userId = {}, reportId = {}인 관심신고글 삭제 완료", userId, reportId);

    }

    private List<ReportProjection> takeWithSize(int size, List<ReportProjection> interestReportProjections) {
        return interestReportProjections.size() > size ? interestReportProjections.subList(0, size) : interestReportProjections;
    }

    private long getLastId(List<ReportProjection> reportProjections) {
        return !reportProjections.isEmpty()? reportProjections.get(reportProjections.size() - 1).getReportId() : -1L;
    }
}
