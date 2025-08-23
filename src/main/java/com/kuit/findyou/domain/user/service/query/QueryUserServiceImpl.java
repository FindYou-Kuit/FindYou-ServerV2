package com.kuit.findyou.domain.user.service.query;

import com.kuit.findyou.domain.user.dto.GetUseProfileResponse;
import com.kuit.findyou.domain.user.dto.request.CheckDuplicateNicknameRequest;
import com.kuit.findyou.domain.user.dto.response.CheckDuplicateNicknameResponse;
import com.kuit.findyou.domain.user.model.User;
import com.kuit.findyou.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class QueryUserServiceImpl implements QueryUserService {
    private final UserRepository userRepository;

    @Override
    public CheckDuplicateNicknameResponse checkDuplicateNickname(CheckDuplicateNicknameRequest request) {
        boolean exists = userRepository.existsByName(request.nickname());
        log.info("[checkDuplicateNickname] result = {}", exists);
        return new CheckDuplicateNicknameResponse(exists);
    }

    @Override
    public GetUseProfileResponse getUserProfile(Long userId) {
        log.info("[getUserProfile] userId = {}", userId);
        User user = userRepository.getReferenceById(userId);
        return new GetUseProfileResponse(user.getName(), user.getProfileImageUrl());
    }
}
