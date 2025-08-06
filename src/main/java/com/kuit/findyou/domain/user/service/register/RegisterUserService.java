package com.kuit.findyou.domain.user.service.register;

import com.kuit.findyou.domain.user.dto.RegisterUserRequest;
import com.kuit.findyou.domain.user.dto.RegisterUserResponse;

public interface RegisterUserService {
    RegisterUserResponse registerUser(RegisterUserRequest request);
}
