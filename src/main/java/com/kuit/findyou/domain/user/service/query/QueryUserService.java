package com.kuit.findyou.domain.user.service.query;

import com.kuit.findyou.domain.user.dto.GetUseProfilerResponse;
import com.kuit.findyou.domain.user.dto.request.CheckDuplicateNicknameRequest;
import com.kuit.findyou.domain.user.dto.response.CheckDuplicateNicknameResponse;

public interface QueryUserService {
    CheckDuplicateNicknameResponse checkDuplicateNickname(CheckDuplicateNicknameRequest request);

    GetUseProfilerResponse getUserProfile(Long userId);
}
