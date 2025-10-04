package com.kuit.findyou.global.external.client;

import com.kuit.findyou.global.external.constant.ExternalExceptionMessage;
import com.kuit.findyou.global.external.dto.MissingAnimalApiFullResponse;
import com.kuit.findyou.global.external.dto.MissingAnimalItemDTO;
import com.kuit.findyou.global.external.exception.MissingAnimalApiClientException;
import com.kuit.findyou.global.external.properties.LossAnimalInfoProperties;
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

import static com.kuit.findyou.global.external.constant.ExternalExceptionMessage.*;
import static com.kuit.findyou.global.external.constant.ExternalExceptionMessage.PROTECTING_ANIMAL_API_CLIENT_EMPTY_RESPONSE;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ActiveProfiles("test")
@ExtendWith(org.mockito.junit.jupiter.MockitoExtension.class)
class MissingAnimalApiClientTest {

    @Mock LossAnimalInfoProperties properties;
    @Mock RestClient restClient;
    @Mock RestClient.RequestHeadersUriSpec<?> uriSpec;
    @Mock RestClient.ResponseSpec responseSpec;

    MissingAnimalApiClient client;

    @BeforeEach
    void setUp() {
        client = new MissingAnimalApiClient(properties, restClient);
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
    private MissingAnimalApiFullResponse deepResponse(List<MissingAnimalItemDTO> items, String totalCount) {
        MissingAnimalApiFullResponse full =
                mock(MissingAnimalApiFullResponse.class, Answers.RETURNS_DEEP_STUBS);
        when(full.response().body().items().item()).thenReturn(items);
        when(full.response().body().totalCount()).thenReturn(totalCount);
        return full;
    }

    @Test
    @DisplayName("단일 페이지: totalCount=2 → 1페이지만 수집")
    void fetchAll_singlePage() {
        stubChain();

        var i1 = mock(MissingAnimalItemDTO.class);
        var i2 = mock(MissingAnimalItemDTO.class);
        var page1 = deepResponse(List.of(i1, i2), "2"); // pageSize=1000 → totalPages=1

        when(responseSpec.body(MissingAnimalApiFullResponse.class)).thenReturn(page1);

        var result = client.fetchAllMissingAnimals("20240101", "20240131");

        assertThat(result).hasSize(2).containsExactly(i1, i2);
    }

    @Test
    @DisplayName("다중 페이지: totalCount=2000 → 2페이지 수집 후 종료")
    void fetchAll_multiPage() {
        stubChain();

        var a = mock(MissingAnimalItemDTO.class);
        var b = mock(MissingAnimalItemDTO.class);
        var c = mock(MissingAnimalItemDTO.class);

        var page1 = deepResponse(List.of(a, b), "2000"); // totalPages=2
        var page2 = deepResponse(List.of(c), "2000");

        when(responseSpec.body(MissingAnimalApiFullResponse.class)).thenReturn(page1, page2);

        var result = client.fetchAllMissingAnimals("20240101", "20240131");

        assertThat(result).hasSize(3).containsExactly(a, b, c);
    }

    @Test
    @DisplayName("빈 응답: items.item() == null → 예외 발생(EMPTY_RESPONSE)")
    void fetchAll_emptyResponse_throws() {
        stubChain();

        var empty = mock(MissingAnimalApiFullResponse.class, Answers.RETURNS_DEEP_STUBS);
        when(empty.response().body().items().item()).thenReturn(null);

        when(responseSpec.body(MissingAnimalApiFullResponse.class)).thenReturn(empty);

        assertThatThrownBy(() -> client.fetchAllMissingAnimals("20240101", "20240131"))
                .isInstanceOf(MissingAnimalApiClientException.class)
                .hasMessage(MISSING_ANIMAL_API_CLIENT_EMPTY_RESPONSE.getValue());
    }

    @Test
    @DisplayName("중간 예외: 1페이지 수집 후 2페이지에서 예외 → MissingAnimalApiClientException(CALL_FAILED)")
    void fetchAll_exceptionOnSecondPage_throws() {
        stubChain();

        var i1 = mock(MissingAnimalItemDTO.class);
        var page1 = deepResponse(List.of(i1), "2000"); // totalPages=2

        when(responseSpec.body(MissingAnimalApiFullResponse.class))
                .thenReturn(page1)                         // 1페이지 OK
                .thenThrow(new RuntimeException("timeout"));// 2페이지에서 예외

        assertThatThrownBy(() -> client.fetchAllMissingAnimals("20240101", "20240131"))
                .isInstanceOf(MissingAnimalApiClientException.class)
                .hasMessage(MISSING_ANIMAL_API_CLIENT_CALL_FAILED.getValue());
    }

    @Test
    @DisplayName("빈 응답: body()가 아예 null → 예외 발생(EMPTY_RESPONSE)")
    void fetchAll_responseNull_throws() {
        stubChain();
        var full = mock(MissingAnimalApiFullResponse.class, Answers.RETURNS_DEEP_STUBS);
        when(full.response()).thenReturn(null);

        when(responseSpec.body(MissingAnimalApiFullResponse.class)).thenReturn(full);

        assertThatThrownBy(() -> client.fetchAllMissingAnimals("20240101", "20240131"))
                .isInstanceOf(MissingAnimalApiClientException.class)
                .hasMessage(MISSING_ANIMAL_API_CLIENT_EMPTY_RESPONSE.getValue());
    }

    @Test
    @DisplayName("빈 응답: responseSpec.body(...) 자체가 null → 예외 발생(EMPTY_RESPONSE)")
    void fetchAll_bodyCallReturnsNull_throws() {
        stubChain();
        when(responseSpec.body(MissingAnimalApiFullResponse.class)).thenReturn(null);

        assertThatThrownBy(() -> client.fetchAllMissingAnimals("20240101", "20240131"))
                .isInstanceOf(MissingAnimalApiClientException.class)
                .hasMessage(MISSING_ANIMAL_API_CLIENT_EMPTY_RESPONSE.getValue());
    }

    @Test
    @DisplayName("빈 응답: items()가 null → 예외 발생(EMPTY_RESPONSE)")
    void fetchAll_itemsNull_throws() {
        stubChain();
        var full = mock(MissingAnimalApiFullResponse.class, Answers.RETURNS_DEEP_STUBS);
        when(full.response().body().items()).thenReturn(null);

        when(responseSpec.body(MissingAnimalApiFullResponse.class)).thenReturn(full);

        assertThatThrownBy(() -> client.fetchAllMissingAnimals("20240101", "20240131"))
                .isInstanceOf(MissingAnimalApiClientException.class)
                .hasMessage(MISSING_ANIMAL_API_CLIENT_EMPTY_RESPONSE.getValue());
    }

    @Test
    @DisplayName("경계값: totalCount=0, items 빈 리스트 → 예외 없이 빈 리스트 반환")
    void fetchAll_totalCountZero_returnsEmpty() {
        stubChain();
        var page1 = deepResponse(List.of(), "0"); // 구조는 정상, 데이터만 없음

        when(responseSpec.body(MissingAnimalApiFullResponse.class)).thenReturn(page1);

        var result = client.fetchAllMissingAnimals("20240101", "20240131");
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("경계값: totalCount=1000(페이지 사이즈의 배수) → 딱 1페이지만")
    void fetchAll_totalCountExactPageSize_onePage() {
        stubChain();
        var i1 = mock(MissingAnimalItemDTO.class);
        var page1 = deepResponse(List.of(i1), "1000");

        when(responseSpec.body(MissingAnimalApiFullResponse.class)).thenReturn(page1);

        var result = client.fetchAllMissingAnimals("20240101", "20240131");
        assertThat(result).containsExactly(i1);
    }
}
