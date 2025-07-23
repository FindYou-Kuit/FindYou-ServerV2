package com.kuit.findyou.domain.user.service.viewed_reports;

import com.kuit.findyou.domain.report.dto.response.CardResponseDTO;

public interface ViewedReportsRetrieveService {

    CardResponseDTO retrieveViewedAnimals(
            Long lastId,
            Long userId
    );
}
