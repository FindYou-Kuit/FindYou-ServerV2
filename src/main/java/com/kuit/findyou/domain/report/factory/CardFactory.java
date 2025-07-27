package com.kuit.findyou.domain.report.factory;

import com.kuit.findyou.domain.report.dto.response.Card;
import com.kuit.findyou.domain.report.dto.response.CardResponseDTO;
import com.kuit.findyou.domain.report.dto.response.ReportProjection;
import com.kuit.findyou.domain.report.model.ReportTag;
import com.kuit.findyou.domain.report.repository.InterestReportRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class CardFactory {

    private final InterestReportRepository interestReportRepository;

    public CardResponseDTO createCardResponse(
            List<ReportProjection> projections,
            Long userId,
            Long lastId,
            boolean isLast
    ) {
        List<Long> reportIds = projections.stream()
                .map(ReportProjection::getReportId)
                .toList();

        Set<Long> interestIds = new HashSet<>(interestReportRepository.findInterestedReportIdsByUserIdAndReportIds(userId, reportIds));

        List<Card> cards = projections.stream()
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

        return new CardResponseDTO(cards, lastId, isLast);
    }
}
