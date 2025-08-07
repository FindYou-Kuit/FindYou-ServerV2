package com.kuit.findyou.domain.user.service.facade;

import com.kuit.findyou.domain.report.dto.response.CardResponseDTO;
import com.kuit.findyou.domain.user.dto.*;
import com.kuit.findyou.domain.user.service.interest_report.InterestReportService;
import com.kuit.findyou.domain.user.service.query.QueryUserService;
import com.kuit.findyou.domain.user.service.register.RegisterUserService;
import com.kuit.findyou.domain.user.service.viewed_reports.ViewedReportsRetrieveService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserServiceFacade {
    private final InterestReportService interestReportService;
    private final ViewedReportsRetrieveService viewedReportsRetrieveService;
    private final RegisterUserService registerUserService;
    private final QueryUserService queryUserService;

    public CardResponseDTO retrieveViewedAnimals(Long lastId, Long userId) {
        return viewedReportsRetrieveService.retrieveViewedAnimals(lastId, userId);
    }

    public RegisterUserResponse registerUser(RegisterUserRequest request) {
        return registerUserService.registerUser(request);
    }

    public CheckDuplicateNicknameResponse checkDuplicateNickname(CheckDuplicateNicknameRequest request) {
        return queryUserService.checkDuplicateNickname(request);
    }

    public RetrieveInterestAnimalsResponse retrieveInterestAnimals(Long userId, Long lastId) {
        return interestReportService.retrieveInterestAnimals(userId, lastId, 20);
    }
}
