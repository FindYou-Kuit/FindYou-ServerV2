package com.kuit.findyou.domain.home.service.card;

import com.kuit.findyou.domain.home.dto.response.ProtectingAnimalCard;
import com.kuit.findyou.domain.home.dto.response.WitnessedOrMissingAnimalCard;
import com.kuit.findyou.domain.report.dto.response.ReportProjection;
import com.kuit.findyou.domain.report.model.ReportTag;
import com.kuit.findyou.domain.report.repository.ReportRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Service
public class RetrieveHomeAnimalCardServiceImpl implements RetrieveHomeAnimalCardService {
    private final ReportRepository reportRepository;
    public List<ProtectingAnimalCard> retrieveProtectingReportCards(Double latitude, Double longitude, int size) {
        List<ReportTag> tags = List.of(ReportTag.PROTECTING);
        List<ReportProjection> nearestReports = reportRepository.findNearestReports(latitude, longitude, tags, PageRequest.of(0, size));
        return nearestReports.stream()
                .map(ProtectingAnimalCard::of)
                .collect(Collectors.toList());
    }

    public List<WitnessedOrMissingAnimalCard> retrieveWitnessedOrMissingReportCards(Double latitude, Double longitude, int size) {
        List<ReportTag> tags = List.of(ReportTag.WITNESS, ReportTag.MISSING);
        List<ReportProjection> nearestReports = reportRepository.findNearestReports(latitude, longitude, tags, PageRequest.of(0, size));
        return nearestReports.stream()
                .map(WitnessedOrMissingAnimalCard::of)
                .collect(Collectors.toList());
    }

    public List<ProtectingAnimalCard> retrieveProtectingReportCards(int size) {
        List<ReportProjection> content = reportRepository.findReportsWithFilters(List.of(ReportTag.PROTECTING), null, null, null, null, null, Long.MAX_VALUE, PageRequest.of(0, size)).getContent();
        return content.stream()
                .map(ProtectingAnimalCard::of)
                .collect(Collectors.toList());
    }

    public List<WitnessedOrMissingAnimalCard> retrieveWitnessedOrMissingReportCards(int size) {
        List<ReportProjection> content = reportRepository.findReportsWithFilters(List.of(ReportTag.WITNESS, ReportTag.MISSING), null, null, null, null, null, Long.MAX_VALUE, PageRequest.of(0, size)).getContent();
        return content.stream()
                .map(WitnessedOrMissingAnimalCard::of)
                .collect(Collectors.toList());
    }
}
