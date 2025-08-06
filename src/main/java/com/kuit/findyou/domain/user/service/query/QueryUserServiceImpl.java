package com.kuit.findyou.domain.user.service.query;

import com.kuit.findyou.domain.user.dto.CheckDuplicateNicknameRequest;
import com.kuit.findyou.domain.user.dto.CheckDuplicateNicknameResponse;
import com.kuit.findyou.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;


@Slf4j
@RequiredArgsConstructor
@Service
public class QueryUserServiceImpl implements QueryUserService {
    private final UserRepository userRepository;

    @Override
    public CheckDuplicateNicknameResponse checkDuplicateNickname(CheckDuplicateNicknameRequest request) {
        boolean exists = userRepository.existsByName(request.nickname());
        log.info("[checkDuplicateNickname] result = {}", exists);
        return new CheckDuplicateNicknameResponse(exists);
    }
}
