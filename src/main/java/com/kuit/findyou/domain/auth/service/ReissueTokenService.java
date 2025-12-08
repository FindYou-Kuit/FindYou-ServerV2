package com.kuit.findyou.domain.auth.service;

import com.kuit.findyou.domain.auth.dto.ReissueTokenRequest;
import com.kuit.findyou.domain.auth.dto.ReissueTokenResponse;

public interface ReissueTokenService {
    ReissueTokenResponse reissueToken(ReissueTokenRequest request);
}
