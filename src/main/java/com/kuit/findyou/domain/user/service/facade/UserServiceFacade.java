package com.kuit.findyou.domain.user.service.facade;

import com.kuit.findyou.domain.report.dto.response.CardResponseDTO;
import com.kuit.findyou.domain.user.service.viewed_reports.ViewedReportsRetrieveService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserServiceFacade {

    private final ViewedReportsRetrieveService viewedReportsRetrieveService;

    public CardResponseDTO retrieveViewedAnimals(Long lastId, Long userId) {
        return viewedReportsRetrieveService.retrieveViewedAnimals(lastId, userId);
    }
}
