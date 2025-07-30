package com.kuit.findyou.domain.city.service.facade;

import com.kuit.findyou.domain.city.dto.response.SidoListResponseDTO;
import com.kuit.findyou.domain.city.service.sidoQuery.SidoQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class CityServiceFacade {

    private final SidoQueryService sidoQueryService;

    public SidoListResponseDTO getSidoNames() {
        return sidoQueryService.getSidoList();
    }

}
