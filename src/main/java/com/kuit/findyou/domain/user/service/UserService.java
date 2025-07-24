package com.kuit.findyou.domain.user.service;

import com.kuit.findyou.domain.user.dto.CheckDuplicateNicknameRequest;
import com.kuit.findyou.domain.user.dto.CheckDuplicateNicknameResponse;
import com.kuit.findyou.domain.user.dto.RegisterUserRequest;
import com.kuit.findyou.domain.user.dto.RegisterUserResponse;

public interface UserService {
    RegisterUserResponse registerUser(RegisterUserRequest request);

    CheckDuplicateNicknameResponse checkDuplicateNickname(CheckDuplicateNicknameRequest request);
}
