package com.kuit.findyou.global.external.client;

import com.kuit.findyou.global.external.dto.ProtectingAnimalApiFullResponse;
import com.kuit.findyou.global.external.dto.ProtectingAnimalItemDTO;
import com.kuit.findyou.global.external.exception.ProtectingAnimalApiClientException;
import com.kuit.findyou.global.external.properties.ProtectingAnimalApiProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriBuilder;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
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

    @SuppressWarnings("unchecked")
    private void stubChain() {
        doReturn(uriSpec).when(restClient).get();
        doAnswer(inv -> {
            Function<UriBuilder, URI> fn = inv.getArgument(0);
            fn.apply(UriComponentsBuilder.fromHttpUrl("http://localhost"));
            return uriSpec;
        }).when(uriSpec).uri(any(Function.class));
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
    @DisplayName("빈 응답: items.item() == null → 예외 발생(EMPTY_RESPONSE)")
    void fetchAll_emptyResponse_throws() {
        stubChain();

        var empty = mock(ProtectingAnimalApiFullResponse.class, Answers.RETURNS_DEEP_STUBS);
        when(empty.response().body().items().item()).thenReturn(null);

        when(responseSpec.body(ProtectingAnimalApiFullResponse.class)).thenReturn(empty);

        assertThatThrownBy(() -> client.fetchAllProtectingAnimals())
                .isInstanceOf(ProtectingAnimalApiClientException.class);
    }

    @Test
    @DisplayName("중간 예외: 1페이지 수집 후 2페이지에서 예외 → ProtectingAnimalApiClientException(CALL_FAILED)")
    void fetchAll_exceptionOnSecondPage_throws() {
        stubChain();

        var i1 = mock(ProtectingAnimalItemDTO.class);
        var page1 = deepResponse(List.of(i1), "2000"); // totalPages=2

        when(responseSpec.body(ProtectingAnimalApiFullResponse.class))
                .thenReturn(page1)                          // 1페이지 OK
                .thenThrow(new RuntimeException("timeout"));// 2페이지 예외

        assertThatThrownBy(() -> client.fetchAllProtectingAnimals())
                .isInstanceOf(ProtectingAnimalApiClientException.class);
    }

    @Test
    @DisplayName("빈 응답: response()가 null → 예외 발생(EMPTY_RESPONSE)")
    void fetchAll_responseNull_throws() {
        stubChain();
        var full = mock(ProtectingAnimalApiFullResponse.class, Answers.RETURNS_DEEP_STUBS);
        when(full.response()).thenReturn(null);

        when(responseSpec.body(ProtectingAnimalApiFullResponse.class)).thenReturn(full);

        assertThatThrownBy(() -> client.fetchAllProtectingAnimals())
                .isInstanceOf(ProtectingAnimalApiClientException.class);
    }

    @Test
    @DisplayName("빈 응답: responseSpec.body(...) 자체가 null → 예외 발생(EMPTY_RESPONSE)")
    void fetchAll_bodyCallReturnsNull_throws() {
        stubChain();
        when(responseSpec.body(ProtectingAnimalApiFullResponse.class)).thenReturn(null);

        assertThatThrownBy(() -> client.fetchAllProtectingAnimals())
                .isInstanceOf(ProtectingAnimalApiClientException.class);
    }

    @Test
    @DisplayName("빈 응답: items()가 null → 예외 발생(EMPTY_RESPONSE)")
    void fetchAll_itemsNull_throws() {
        stubChain();
        var full = mock(ProtectingAnimalApiFullResponse.class, Answers.RETURNS_DEEP_STUBS);
        when(full.response().body().items()).thenReturn(null);

        when(responseSpec.body(ProtectingAnimalApiFullResponse.class)).thenReturn(full);

        assertThatThrownBy(() -> client.fetchAllProtectingAnimals())
                .isInstanceOf(ProtectingAnimalApiClientException.class);
    }

    @Test
    @DisplayName("경계값: totalCount=0, items 빈 리스트 → 수집 없이 종료")
    void fetchAll_totalCountZero_returnsEmptyButPassesLoop() {
        stubChain();
        var page1 = deepResponse(List.of(), "0");

        when(responseSpec.body(ProtectingAnimalApiFullResponse.class)).thenReturn(page1);

        var result = client.fetchAllProtectingAnimals();
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("경계값: totalCount=1000(페이지 사이즈의 배수) → 딱 1페이지만")
    void fetchAll_totalCountExactPageSize_onePage() {
        stubChain();
        var i1 = mock(ProtectingAnimalItemDTO.class);
        var page1 = deepResponse(List.of(i1), "1000");

        when(responseSpec.body(ProtectingAnimalApiFullResponse.class)).thenReturn(page1);

        var result = client.fetchAllProtectingAnimals();
        assertThat(result).containsExactly(i1);
    }

}
