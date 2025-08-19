package com.kuit.findyou.domain.user.service.report;

import com.kuit.findyou.domain.report.dto.response.CardResponseDTO;
import com.kuit.findyou.domain.report.dto.response.ReportProjection;
import com.kuit.findyou.domain.report.factory.CardFactory;
import com.kuit.findyou.domain.report.repository.InterestReportRepository;
import com.kuit.findyou.domain.report.repository.ReportRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserReportServiceImpl implements UserReportService {
    private final ReportRepository reportRepository;
    private final InterestReportRepository interestReportRepository;
    private final CardFactory cardFactory;
    @Override
    public CardResponseDTO retrieveUserReports(Long userId, Long lastId, int size) {
        log.info("[retrieveUserReports] userId = {}, lastId = {}, size = {}", userId, lastId, size);

        // userId로 신고글 조회
        Slice<ReportProjection> slices = reportRepository.findUserReportsByCusor(userId, lastId, PageRequest.of(0, size));

        // 페이징 결과를 반환
        List<ReportProjection> projections = slices.getContent();
        Set<Long> interestIds = getInterestIds(userId, projections);

        return cardFactory.createCardResponse(projections,
                interestIds,
                getLastId(projections),
                !slices.hasNext()
        );
    }

    private Set<Long> getInterestIds(Long userId, List<ReportProjection> projections) {
        List<Long> interestedReportIds = interestReportRepository.findInterestedReportIdsByUserIdAndReportIds(userId,
                projections.stream().map(ReportProjection::getReportId).collect(Collectors.toList()));
        return new HashSet<>(interestedReportIds);
    }

    private static long getLastId(List<ReportProjection> projections) {
        return projections.size() > 0 ? projections.get(projections.size() - 1).getReportId() : -1L;
    }
}
