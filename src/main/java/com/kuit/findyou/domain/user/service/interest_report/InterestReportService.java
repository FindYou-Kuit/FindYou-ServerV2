package com.kuit.findyou.domain.user.service.interest_report;

import com.kuit.findyou.domain.user.dto.RetrieveInterestAnimalsResponse;

public interface InterestReportService {
    RetrieveInterestAnimalsResponse retrieveInterestAnimals(Long userId, Long lastId, int size);
}
