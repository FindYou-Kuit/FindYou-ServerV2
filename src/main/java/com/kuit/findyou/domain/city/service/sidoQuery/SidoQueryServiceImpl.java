package com.kuit.findyou.domain.city.service.sidoQuery;

import com.kuit.findyou.domain.city.dto.response.SidoDTO;
import com.kuit.findyou.domain.city.dto.response.SidoListResponseDTO;
import com.kuit.findyou.domain.city.repository.SidoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SidoQueryServiceImpl implements SidoQueryService {

    private final SidoRepository sidoRepository;

    @Override
    public SidoListResponseDTO getSidoList() {
        return new SidoListResponseDTO(sidoRepository.findAll().stream()
                .map(sido -> new SidoDTO(sido.getId(), sido.getName()))
                .toList()
        );
    }
}
