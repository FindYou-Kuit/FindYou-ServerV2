package com.kuit.findyou.domain.report.factory;

import com.kuit.findyou.domain.report.dto.response.Card;
import com.kuit.findyou.domain.report.dto.response.CardResponseDTO;
import com.kuit.findyou.domain.report.dto.response.ReportProjection;
import com.kuit.findyou.domain.report.model.ReportTag;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class CardFactory {
    public CardResponseDTO createCardResponse(
            List<ReportProjection> projections,
            Set<Long> interestIds,
            Long lastId,
            boolean isLast
    ) {
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
