package com.kuit.findyou.domain.report.service.retrieve;

import com.kuit.findyou.domain.report.dto.request.ReportViewType;
import com.kuit.findyou.domain.report.dto.response.Card;
import com.kuit.findyou.domain.report.dto.response.CardResponseDTO;
import com.kuit.findyou.domain.report.dto.response.ReportProjection;
import com.kuit.findyou.domain.report.model.ReportTag;
import com.kuit.findyou.domain.report.repository.InterestReportRepository;
import com.kuit.findyou.domain.report.repository.ReportRepository;
import com.kuit.findyou.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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

    private final InterestReportRepository interestReportRepository;
    private final ReportRepository reportRepository;

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

        List<Card> reportList = convertReportProjectionSliceToCardVOList(reportSlice.getContent(), userId);

        // 마지막 글의 ID == 다음 요청으로 전달할 Cursor 값
        Long nextCursor = findLastReportId(reportList);

        return new CardResponseDTO(reportList, nextCursor, !reportSlice.hasNext());
    }


    private List<String> parseBreeds(String breeds) {
        if (breeds == null || breeds.isBlank()) return null;
        return Arrays.stream(breeds.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();
    }

    private List<Card> convertReportProjectionSliceToCardVOList(List<ReportProjection> reportSlice, Long userId) {
        List<Long> reportIds = reportSlice.stream()
                .map(ReportProjection::getReportId)
                .toList();

        // 관심 있는 reportId만 조회
        Set<Long> interestIds = new HashSet<>(
                interestReportRepository.findInterestedReportIdsByUserIdAndReportIds(userId, reportIds)
        );

        return reportSlice.stream()
                .map(p -> new Card(
                        p.getReportId(),
                        p.getThumbnailImageUrl(),
                        p.getTitle(),
                        ReportTag.valueOf(p.getTag()).getValue(),
                        p.getDate().toString(),
                        p.getAddress(),
                        interestIds.contains(p.getReportId())
                ))
                .toList();
    }


    private Long findLastReportId(List<Card> reportList) {
        if (reportList.isEmpty()) return -1L;

        return reportList.get(reportList.size() - 1).reportId();
    }
}

