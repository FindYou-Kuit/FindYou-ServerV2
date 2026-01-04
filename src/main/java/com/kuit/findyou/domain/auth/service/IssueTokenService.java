package com.kuit.findyou.domain.auth.service;

import com.kuit.findyou.domain.user.model.Role;

public interface IssueTokenService {
    String issueAccessToken(Long userId, Role role);
    String issueRefreshToken(Long userId);
}
