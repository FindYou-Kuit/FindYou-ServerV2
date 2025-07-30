package com.kuit.findyou.domain.city.service.sidoQuery;

import com.kuit.findyou.domain.city.dto.response.SidoNameResponseDTO;
import com.kuit.findyou.domain.city.model.Sido;
import com.kuit.findyou.domain.city.repository.SidoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SidoQueryServiceImpl implements SidoQueryService {

    private final SidoRepository sidoRepository;

    @Override
    public SidoNameResponseDTO getSidoNames() {
        return new SidoNameResponseDTO(sidoRepository.findAll().stream()
                .map(Sido::getName)
                .toList()
        );
    }
}
