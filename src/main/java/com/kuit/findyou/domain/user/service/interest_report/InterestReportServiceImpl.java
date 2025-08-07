package com.kuit.findyou.domain.user.service.interest_report;

import com.kuit.findyou.domain.report.dto.response.ReportProjection;
import com.kuit.findyou.domain.report.repository.InterestReportRepository;
import com.kuit.findyou.domain.user.dto.RetrieveInterestAnimalsResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service
public class InterestReportServiceImpl implements InterestReportService{
    private final InterestReportRepository interestReportRepository;

    @Override
    public RetrieveInterestAnimalsResponse retrieveInterestAnimals(Long userId, Long lastId, int size) {
        log.info("[retrieveInterestAnimals] userId = {}, lastId = {}, size = {}", userId, lastId, size);
        // 유저가 관심 게시글로 등록한 게시글을 최신순으로 페이징하여 조회

        List<ReportProjection> interestReportProjections = interestReportRepository.findInterestReportsByCursor(userId, lastId, PageRequest.of(0, size + 1));

        return RetrieveInterestAnimalsResponse.from(
                interestReportProjections.subList(0, 21),
                interestReportProjections.isEmpty() ? -1L : interestReportProjections.get(interestReportProjections.size() - 1).getReportId(),
                interestReportProjections.size() > size
        );
    }
}
