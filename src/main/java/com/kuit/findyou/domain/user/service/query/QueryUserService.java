package com.kuit.findyou.domain.user.service.query;

import com.kuit.findyou.domain.user.dto.CheckDuplicateNicknameRequest;
import com.kuit.findyou.domain.user.dto.CheckDuplicateNicknameResponse;

public interface QueryUserService {
    CheckDuplicateNicknameResponse checkDuplicateNickname(CheckDuplicateNicknameRequest request);
}
