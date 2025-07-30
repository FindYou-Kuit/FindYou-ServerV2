package com.kuit.findyou.domain.city.service.sigunguQuery;

import com.kuit.findyou.domain.city.dto.response.SigunguListResponseDTO;
import com.kuit.findyou.domain.city.model.Sigungu;
import com.kuit.findyou.domain.city.repository.SigunguRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SigunguQueryServiceImpl implements SigunguQueryService{

    private final SigunguRepository sigunguRepository;

    @Override
    public SigunguListResponseDTO getSigunguList(Long sidoId) {
        return new SigunguListResponseDTO(sigunguRepository.findBySido_Id(sidoId).stream()
                .map(Sigungu::getName)
                .toList());
    }
}
