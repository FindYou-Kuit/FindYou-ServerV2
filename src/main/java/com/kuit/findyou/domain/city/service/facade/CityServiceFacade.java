package com.kuit.findyou.domain.city.service.facade;

import com.kuit.findyou.domain.city.dto.response.SidoNameResponseDTO;
import com.kuit.findyou.domain.city.service.sidoQuery.SidoQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class CityServiceFacade {

    private final SidoQueryService sidoQueryService;

    public SidoNameResponseDTO getSidoNames() {
        return sidoQueryService.getSidoNames();
    }

}
