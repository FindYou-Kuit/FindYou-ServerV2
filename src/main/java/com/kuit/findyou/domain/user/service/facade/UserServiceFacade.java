package com.kuit.findyou.domain.user.service.facade;

import com.kuit.findyou.domain.report.dto.response.CardResponseDTO;
import com.kuit.findyou.domain.user.dto.request.ChangeProfileImageRequest;
import com.kuit.findyou.domain.user.dto.GetUserProfileResponse;
import com.kuit.findyou.domain.user.dto.request.CheckDuplicateNicknameRequest;
import com.kuit.findyou.domain.user.dto.request.RegisterUserRequest;
import com.kuit.findyou.domain.user.dto.response.CheckDuplicateNicknameResponse;
import com.kuit.findyou.domain.user.dto.response.RegisterUserResponse;
import com.kuit.findyou.domain.user.service.change_nickname.ChangeNicknameService;
import com.kuit.findyou.domain.user.service.change_profileImage.ChangeProfileImageService;
import com.kuit.findyou.domain.user.service.delete.DeleteUserService;
import com.kuit.findyou.domain.user.service.interest_report.InterestReportService;
import com.kuit.findyou.domain.user.service.query.QueryUserService;
import com.kuit.findyou.domain.user.service.register.RegisterUserService;
import com.kuit.findyou.domain.user.service.report.UserReportService;
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
    private final ChangeNicknameService changeNicknameService;
    private final DeleteUserService deleteUserService;
    private final ChangeProfileImageService changeProfileImageService;
    private final UserReportService userReportService;

    public CardResponseDTO retrieveViewedAnimals(Long lastId, Long userId) {
        return viewedReportsRetrieveService.retrieveViewedAnimals(lastId, userId);
    }

    public RegisterUserResponse registerUser(RegisterUserRequest request) {
        return registerUserService.registerUser(request);
    }

    public CheckDuplicateNicknameResponse checkDuplicateNickname(CheckDuplicateNicknameRequest request) {
        return queryUserService.checkDuplicateNickname(request);
    }

    public CardResponseDTO retrieveInterestAnimals(Long userId, Long lastId) {
        return interestReportService.retrieveInterestAnimals(userId, lastId, 20);
    }

    public void addInterestAnimal(Long userId, Long reportId){
        interestReportService.addInterestAnimal(userId, reportId);
    }

    public void deleteInterestAnimal(Long userId, Long reportId){
        interestReportService.deleteInterestAnimal(userId, reportId);
    }

    public void changeNickname(Long userId, String newNickname) {
        changeNicknameService.changeNickname(userId, newNickname);
    }

    public void deleteUser(Long userId) {
        deleteUserService.deleteUser(userId);
    }

    public void changeProfileImage(Long userId, ChangeProfileImageRequest request){ changeProfileImageService.changeProfileImage(userId, request); }

    public CardResponseDTO retrieveUserReports(Long userId, Long lastId){
        return userReportService.retrieveUserReports(userId, lastId, 20);
    }

    public GetUserProfileResponse getUserProfile(Long userId) {
        return queryUserService.getUserProfile(userId);
    }
}
