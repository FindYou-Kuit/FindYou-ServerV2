package com.kuit.findyou.domain.city.service.facade;

import com.kuit.findyou.domain.city.dto.response.SidoListResponseDTO;
import com.kuit.findyou.domain.city.dto.response.SigunguListResponseDTO;
import com.kuit.findyou.domain.city.service.sidoQuery.SidoQueryService;
import com.kuit.findyou.domain.city.service.sigunguQuery.SigunguQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class CityServiceFacade {

    private final SidoQueryService sidoQueryService;
    private final SigunguQueryService sigunguQueryService;

    public SidoListResponseDTO getSidoList() {
        return sidoQueryService.getSidoList();
    }

    public SigunguListResponseDTO getSigunguList(Long sidoId) {
        return sigunguQueryService.getSigunguList(sidoId);
    }

}
