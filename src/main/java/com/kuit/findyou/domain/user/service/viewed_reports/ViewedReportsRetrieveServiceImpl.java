package com.kuit.findyou.domain.user.service.viewed_reports;

import com.kuit.findyou.domain.report.dto.response.CardResponseDTO;
import com.kuit.findyou.domain.report.dto.response.ReportProjection;
import com.kuit.findyou.domain.report.factory.CardFactory;
import com.kuit.findyou.domain.report.model.ViewedReport;
import com.kuit.findyou.domain.report.repository.ReportRepository;
import com.kuit.findyou.domain.report.repository.ViewedReportRepository;
import com.kuit.findyou.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ViewedReportsRetrieveServiceImpl implements ViewedReportsRetrieveService{

    private final ViewedReportRepository viewedReportRepository;
    private final ReportRepository reportRepository;
    private final CardFactory cardFactory;

    @Override
    public CardResponseDTO retrieveViewedReports(Long lastId, Long userId) {
        // 1. 최근 본 ViewedReport 조회 (Slice 방식)
        Slice<ViewedReport> viewedReportSlice = viewedReportRepository.findByUserIdAndIdLessThanOrderByIdDesc(userId, lastId, PageRequest.of(0, 20));

        List<ViewedReport> content = viewedReportSlice.getContent();

        List<Long> reportIds = content.stream()
                .map(vr -> vr.getReport().getId())
                .toList();

        // 2. Projection 으로 Report 정보 조회
        List<ReportProjection> reportProjections = reportRepository.findReportProjectionsByIdIn(reportIds);

        // 3. Projection 을 ViewedReport 순서에 맞게 정렬
        Map<Long, ReportProjection> projectionMap = reportProjections.stream()
                .collect(Collectors.toMap(ReportProjection::getReportId, p -> p));

        List<ReportProjection> sortedProjections = content.stream()
                .map(vr -> projectionMap.get(vr.getReport().getId()))
                .toList();

        // 4. 마지막 최근 본 글 의 ID 계산
        Long lastViewedReportId = content.isEmpty() ? -1L : content.get(content.size() - 1).getId();

        return cardFactory.createCardResponse(
                sortedProjections,
                userId,
                lastViewedReportId,
                !viewedReportSlice.hasNext());
    }

}
