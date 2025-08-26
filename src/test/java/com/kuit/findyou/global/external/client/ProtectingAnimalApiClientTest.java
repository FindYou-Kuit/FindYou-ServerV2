package com.kuit.findyou.global.external.client;

import com.kuit.findyou.global.external.dto.ProtectingAnimalApiFullResponse;
import com.kuit.findyou.global.external.dto.ProtectingAnimalItemDTO;
import com.kuit.findyou.global.external.properties.ProtectingAnimalApiProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.client.RestClient;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ActiveProfiles("test")
@ExtendWith(org.mockito.junit.jupiter.MockitoExtension.class)
class ProtectingAnimalApiClientTest {

    @Mock ProtectingAnimalApiProperties properties;
    @Mock RestClient restClient;
    @Mock RestClient.RequestHeadersUriSpec<?> uriSpec;
    @Mock RestClient.ResponseSpec responseSpec;

    ProtectingAnimalApiClient client;

    @BeforeEach
    void setUp() {
        client = new ProtectingAnimalApiClient(properties, restClient);
    }

    /** RestClient 체인: get -> uri -> header -> retrieve */
    private void stubChain() {
        doReturn(uriSpec).when(restClient).get();
        doReturn(uriSpec).when(uriSpec).uri(any(java.util.function.Function.class));
        doReturn(uriSpec).when(uriSpec).header(eq("Accept"), eq("application/json"));
        doReturn(responseSpec).when(uriSpec).retrieve();
    }

    /** 깊은 중첩 응답을 간단히 만드는 유틸 */
    private ProtectingAnimalApiFullResponse deepResponse(List<ProtectingAnimalItemDTO> items, String totalCount) {
        ProtectingAnimalApiFullResponse full =
                mock(ProtectingAnimalApiFullResponse.class, Answers.RETURNS_DEEP_STUBS);
        when(full.response().body().items().item()).thenReturn(items);
        when(full.response().body().totalCount()).thenReturn(totalCount);
        return full;
    }

    @Test
    @DisplayName("단일 페이지: totalCount=2 → 1페이지만 수집")
    void fetchAll_singlePage() {
        stubChain();

        var i1 = mock(ProtectingAnimalItemDTO.class);
        var i2 = mock(ProtectingAnimalItemDTO.class);
        var page1 = deepResponse(List.of(i1, i2), "2"); // pageSize=1000 → totalPages=1

        when(responseSpec.body(ProtectingAnimalApiFullResponse.class)).thenReturn(page1);

        var result = client.fetchAllProtectingAnimals();

        assertThat(result).hasSize(2).containsExactly(i1, i2);
    }

    @Test
    @DisplayName("다중 페이지: totalCount=2000 → 2페이지 수집 후 종료")
    void fetchAll_multiPage() {
        stubChain();

        var a = mock(ProtectingAnimalItemDTO.class);
        var b = mock(ProtectingAnimalItemDTO.class);
        var c = mock(ProtectingAnimalItemDTO.class);

        var page1 = deepResponse(List.of(a, b), "2000"); // totalPages=2
        var page2 = deepResponse(List.of(c), "2000");

        when(responseSpec.body(ProtectingAnimalApiFullResponse.class)).thenReturn(page1, page2);

        var result = client.fetchAllProtectingAnimals();

        assertThat(result).hasSize(3).containsExactly(a, b, c);
    }

    @Test
    @DisplayName("빈 응답: items.item() == null → 즉시 종료, 빈 리스트 반환")
    void fetchAll_emptyResponse_returnsEmptyList() {
        stubChain();

        var empty = mock(ProtectingAnimalApiFullResponse.class, Answers.RETURNS_DEEP_STUBS);
        when(empty.response().body().items().item()).thenReturn(null);

        when(responseSpec.body(ProtectingAnimalApiFullResponse.class)).thenReturn(empty);

        var result = client.fetchAllProtectingAnimals();

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("중간 예외: 1페이지 수집 후 2페이지에서 예외 → 누적분만 반환")
    void fetchAll_exceptionOnSecondPage_returnsAccumulated() {
        stubChain();

        var i1 = mock(ProtectingAnimalItemDTO.class);
        var page1 = deepResponse(List.of(i1), "2000"); // totalPages=2

        when(responseSpec.body(ProtectingAnimalApiFullResponse.class))
                .thenReturn(page1)                         // 1페이지 OK
                .thenThrow(new RuntimeException("timeout")); // 2페이지 예외

        var result = client.fetchAllProtectingAnimals();

        assertThat(result).containsExactly(i1);
    }
}
