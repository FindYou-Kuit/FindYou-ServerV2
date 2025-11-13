package com.kuit.findyou.domain.city.service.sidoQuery;

import com.kuit.findyou.domain.city.dto.response.SidoListResponseDTO;
import com.kuit.findyou.domain.city.model.Sido;
import com.kuit.findyou.domain.city.repository.SidoRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
class SidoQueryServiceImplTest {

    @Mock
    SidoRepository sidoRepository;

    @InjectMocks
    SidoQueryServiceImpl sidoQueryService;

    @Test
    @DisplayName("전체 시/도 리스트 반환")
    void getSidoList_success() {
        // given
        List<Sido> sidos = List.of(
                Sido.builder().id(1L).name("서울특별시").build(),
                Sido.builder().id(2L).name("부산광역시").build()
        );

        when(sidoRepository.findAll()).thenReturn(sidos);

        // when
        SidoListResponseDTO result = sidoQueryService.getSidoList();

        // then
        assertThat(result.sidoList())
                .hasSize(2)
                .extracting("name")
                .containsExactly("서울특별시", "부산광역시");
    }
}
