package com.kuit.findyou.domain.breed.service.breedQuery;

import com.kuit.findyou.domain.breed.dto.response.BreedListResponseDTO;
import com.kuit.findyou.domain.breed.model.Breed;
import com.kuit.findyou.domain.breed.repository.BreedRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.kuit.findyou.domain.breed.model.Species.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
class BreedQueryServiceImplTest {

    @Mock
    BreedRepository breedRepository;

    @InjectMocks
    BreedQueryServiceImpl breedQueryService;

    @Test
    @DisplayName("품종 리스트를 종(species)별로 분류하여 반환")
    void getBreedList_success() {
        // given
        List<Breed> breeds = List.of(
                createBreed("진돗개", DOG.getValue()),
                createBreed("포메라니안", DOG.getValue()),
                createBreed("코리안 숏헤어", CAT.getValue()),
                createBreed("기타축종", ETC.getValue())
        );

        when(breedRepository.findAll()).thenReturn(breeds);

        // when
        BreedListResponseDTO response = breedQueryService.getBreedList();

        // then
        assertThat(response.dogBreedList()).containsExactlyInAnyOrder("진돗개", "포메라니안");
        assertThat(response.catBreedList()).containsExactly("코리안 롱헤어");
        assertThat(response.etcBreedList()).containsExactly("기타축종");
    }

    @Test
    @DisplayName("품종 데이터가 없을 경우 빈 리스트 반환")
    void getBreedList_empty() {
        when(breedRepository.findAll()).thenReturn(List.of());

        BreedListResponseDTO response = breedQueryService.getBreedList();

        assertThat(response.dogBreedList()).isEmpty();
        assertThat(response.catBreedList()).isEmpty();
        assertThat(response.etcBreedList()).isEmpty();
    }

    private Breed createBreed(String name, String species) {
        return Breed.builder()
                .name(name)
                .species(species)
                .build();
    }
}
