package com.kuit.findyou.domain.user.service.register;

import com.kuit.findyou.domain.user.dto.request.RegisterUserRequest;
import com.kuit.findyou.domain.user.dto.response.RegisterUserResponse;

public interface RegisterUserService {
    RegisterUserResponse registerUser(RegisterUserRequest request);
}
