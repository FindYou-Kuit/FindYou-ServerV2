package com.kuit.findyou.domain.report.service.retrieve;

import com.kuit.findyou.domain.report.dto.request.ReportViewType;
import com.kuit.findyou.domain.report.dto.request.RetrieveReportRequestDTO;
import com.kuit.findyou.domain.report.dto.response.CardResponseDTO;
import com.kuit.findyou.domain.report.dto.response.ReportProjection;
import com.kuit.findyou.domain.report.factory.CardFactory;
import com.kuit.findyou.domain.report.model.ReportTag;
import com.kuit.findyou.domain.report.repository.InterestReportRepository;
import com.kuit.findyou.domain.report.repository.ReportRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@RequiredArgsConstructor
@Service
@Transactional(readOnly = true)
public class ReportRetrieveServiceImpl implements ReportRetrieveService {

    private final ReportRepository reportRepository;
    private final InterestReportRepository interestReportRepository;
    private final CardFactory cardFactory;

    @Override
    public CardResponseDTO retrieveReportsWithFilters(
            RetrieveReportRequestDTO request,
            Long userId
    ) {
        List<String> breedList = parseBreeds(request.breeds());

        // ReportViewType에 따라 필터링할 tag 목록 생성
        List<ReportTag> tags = createTagList(request.type());

        Slice<ReportProjection> reportSlice = reportRepository.findReportsWithFilters(
                tags, request.startDate(), request.endDate(), request.species(), breedList, request.address(), request.lastId(), PageRequest.of(0, 20)
        );

        Long lastId = findLastId(reportSlice);

        Set<Long> interestIds = findInterestIds(userId, reportSlice);

        return cardFactory.createCardResponse(
                reportSlice.getContent(),
                interestIds,
                lastId,
                !reportSlice.hasNext()
        );
    }

    private Set<Long> findInterestIds(Long userId, Slice<ReportProjection> reportSlice) {
        List<Long> interestIds = interestReportRepository.findInterestedReportIdsByUserIdAndReportIds(userId,
                reportSlice.getContent()
                        .stream()
                        .map(ReportProjection::getReportId)
                        .toList());
        return new HashSet<>(interestIds);
    }

    private List<ReportTag> createTagList(ReportViewType reportViewType) {
        return switch (reportViewType) {
            case ALL -> null;
            case PROTECTING -> List.of(ReportTag.PROTECTING);
            case REPORTING -> List.of(ReportTag.MISSING, ReportTag.WITNESS);
        };
    }

    private List<String> parseBreeds(String breeds) {
        if (breeds == null || breeds.isBlank()) return null;
        return Arrays.stream(breeds.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();
    }

    private Long findLastId(Slice<ReportProjection> reportSlice) {
        return reportSlice.isEmpty() ? -1L : reportSlice.getContent().get(reportSlice.getNumberOfElements()-1).getReportId();
    }
}

