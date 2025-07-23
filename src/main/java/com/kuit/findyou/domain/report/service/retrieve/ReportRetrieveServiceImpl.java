package com.kuit.findyou.domain.report.service.retrieve;

import com.kuit.findyou.domain.report.dto.request.ReportViewType;
import com.kuit.findyou.domain.report.dto.response.Card;
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
    private final CardFactory cardFactory;

    @Override
    public CardResponseDTO retrieveReportsWithFilters(
            ReportViewType reportViewType,
            LocalDate startDate,
            LocalDate endDate,
            String species,
            String breeds,
            String location,
            Long lastReportId,
            Long userId
    ) {
        List<String> breedList = parseBreeds(breeds);

        // ReportViewType에 따라 필터링할 tag 목록 생성
        List<ReportTag> tags = switch (reportViewType) {
            case ALL -> null;
            case PROTECTING -> List.of(ReportTag.PROTECTING);
            case REPORTING -> List.of(ReportTag.MISSING, ReportTag.WITNESS);
        };

        Slice<ReportProjection> reportSlice = reportRepository.findReportsWithFilters(
                tags, startDate, endDate, species, breedList, location, lastReportId, PageRequest.of(0, 20)
        );

        return cardFactory.createCardResponse(
                reportSlice.getContent(),
                userId,
                !reportSlice.hasNext()
        );
    }


    private List<String> parseBreeds(String breeds) {
        if (breeds == null || breeds.isBlank()) return null;
        return Arrays.stream(breeds.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();
    }
}

