package com.kuit.findyou.domain.city.service.sigunguQuery;

import com.kuit.findyou.domain.city.dto.response.SigunguListResponseDTO;
import com.kuit.findyou.domain.city.model.Sido;
import com.kuit.findyou.domain.city.model.Sigungu;
import com.kuit.findyou.domain.city.repository.SidoRepository;
import com.kuit.findyou.domain.city.repository.SigunguRepository;
import com.kuit.findyou.global.common.exception.CustomException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.kuit.findyou.global.common.response.status.BaseExceptionResponseStatus.SIDO_NOT_FOUND;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
class SigunguQueryServiceImplTest {

    @Mock
    SigunguRepository sigunguRepository;

    @Mock
    SidoRepository sidoRepository;

    @InjectMocks
    SigunguQueryServiceImpl sigunguQueryService;

    private final Long sidoId = 1L;

    @Test
    @DisplayName("시/도 id 를 기반으로 해당 시/도에 속한 시/군/구 리스트 조회 성공")
    void getSigunguList_success() {
        // given
        Sido sido = Sido.builder().id(sidoId).name("서울특별시").build();

        List<Sigungu> sigungus = List.of(
                Sigungu.builder().name("광진구").sido(sido).build(),
                Sigungu.builder().name("마포구").sido(sido).build(),
                Sigungu.builder().name("영등포구").sido(sido).build()
        );

        when(sidoRepository.existsById(sidoId)).thenReturn(true);
        when(sigunguRepository.findBySidoId(sidoId)).thenReturn(sigungus);

        // when
        SigunguListResponseDTO response = sigunguQueryService.getSigunguList(sidoId);

        // then
        assertThat(response.sigunguList()).containsExactly("광진구", "마포구", "영등포구");
    }

    @Test
    @DisplayName("시/도 id가 존재하지 않으면 CustomException 발생")
    void getSigunguList_sidoNotFound() {
        // given
        when(sidoRepository.existsById(sidoId)).thenReturn(false);

        // expect
        assertThatThrownBy(() -> sigunguQueryService.getSigunguList(sidoId))
                .isInstanceOf(CustomException.class)
                .hasMessage(SIDO_NOT_FOUND.getMessage());
    }

}