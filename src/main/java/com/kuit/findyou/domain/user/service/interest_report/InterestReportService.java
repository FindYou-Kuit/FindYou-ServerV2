package com.kuit.findyou.domain.user.service.interest_report;

import com.kuit.findyou.domain.report.dto.response.CardResponseDTO;

public interface InterestReportService {
    CardResponseDTO retrieveInterestAnimals(Long userId, Long lastId, int size);

    void addInterestAnimal(Long userId, Long reportId);

    void deleteInterestAnimal(Long userId, Long reportId);
}
