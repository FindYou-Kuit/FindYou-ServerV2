package com.kuit.findyou.domain.user.dto;

import com.kuit.findyou.domain.report.dto.response.ReportProjection;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

public record RetrieveInterestAnimalsResponse(
        List<InterestAnimalPreview> interestAnimals,
        long lastId,
        boolean isLast
) {
    public static RetrieveInterestAnimalsResponse from(List<ReportProjection> reportProjections, long lastId, boolean isLast) {
        return new RetrieveInterestAnimalsResponse(reportProjections.stream()
                .map(InterestAnimalPreview::from)
                .collect(Collectors.toList()),
                lastId,
                isLast);
    }

    public record InterestAnimalPreview(
            Long reportId,
            String thumbnailImageUrl,
            String title,
            String tag,
            LocalDate date,
            String address
    ){
        public static InterestAnimalPreview from(ReportProjection proj){
            return new InterestAnimalPreview(proj.getReportId(), proj.getThumbnailImageUrl(), proj.getTitle(), proj.getTag(), proj.getDate(), proj.getAddress());
        }

    }
}
