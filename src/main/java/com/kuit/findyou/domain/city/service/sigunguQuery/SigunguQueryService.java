package com.kuit.findyou.domain.city.service.sigunguQuery;

import com.kuit.findyou.domain.city.dto.response.SigunguListResponseDTO;

public interface SigunguQueryService {

    SigunguListResponseDTO getSigunguList(Long sidoId);
}
