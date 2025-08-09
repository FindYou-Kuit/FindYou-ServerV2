package com.kuit.findyou.domain.user.service.interest_report;

import com.kuit.findyou.domain.report.dto.response.CardResponseDTO;
import com.kuit.findyou.domain.report.dto.response.ReportProjection;
import com.kuit.findyou.domain.report.factory.CardFactory;
import com.kuit.findyou.domain.report.repository.InterestReportRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Service
public class InterestReportServiceImpl implements InterestReportService{
    private final InterestReportRepository interestReportRepository;
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
                interestReportProjections.size() > size
            );
    }

    private List<ReportProjection> takeWithSize(int size, List<ReportProjection> interestReportProjections) {
        return interestReportProjections.size() > size ? interestReportProjections.subList(0, size) : interestReportProjections;
    }

    private long getLastId(List<ReportProjection> reportProjections) {
        return !reportProjections.isEmpty()? reportProjections.get(reportProjections.size() - 1).getReportId() : -1L;
    }
}
