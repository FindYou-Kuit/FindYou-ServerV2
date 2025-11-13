package com.kuit.findyou.domain.city.service.sigunguQuery;

import com.kuit.findyou.domain.city.dto.response.SigunguListResponseDTO;
import com.kuit.findyou.domain.city.model.Sigungu;
import com.kuit.findyou.domain.city.repository.SidoRepository;
import com.kuit.findyou.domain.city.repository.SigunguRepository;
import com.kuit.findyou.global.common.exception.CustomException;
import com.kuit.findyou.global.common.response.status.BaseExceptionResponseStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import static com.kuit.findyou.global.common.response.status.BaseExceptionResponseStatus.*;

@Service
@RequiredArgsConstructor
public class SigunguQueryServiceImpl implements SigunguQueryService {

    private final SigunguRepository sigunguRepository;
    private final SidoRepository sidoRepository;

    @Override
    public SigunguListResponseDTO getSigunguList(Long sidoId) {
        if (!sidoRepository.existsById(sidoId)) {
            throw new CustomException(SIDO_NOT_FOUND);
        }

        return new SigunguListResponseDTO(sigunguRepository.findBySidoId(sidoId).stream()
                .map(Sigungu::getName)
                .toList());
    }
}
