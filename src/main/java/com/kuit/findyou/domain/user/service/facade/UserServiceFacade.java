package com.kuit.findyou.domain.user.service.facade;

import com.kuit.findyou.domain.report.dto.response.CardResponseDTO;
import com.kuit.findyou.domain.user.dto.CheckDuplicateNicknameRequest;
import com.kuit.findyou.domain.user.dto.CheckDuplicateNicknameResponse;
import com.kuit.findyou.domain.user.dto.RegisterUserRequest;
import com.kuit.findyou.domain.user.dto.RegisterUserResponse;
import com.kuit.findyou.domain.user.service.query.QueryUserService;
import com.kuit.findyou.domain.user.service.register.RegisterUserService;
import com.kuit.findyou.domain.user.service.viewed_reports.ViewedReportsRetrieveService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserServiceFacade {

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
}
