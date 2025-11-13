package com.kuit.findyou.global.external.client;

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

import static com.kuit.findyou.global.external.constant.ExternalExceptionMessage.MISSING_ANIMAL_API_CLIENT_CALL_FAILED;
import static com.kuit.findyou.global.external.constant.ExternalExceptionMessage.MISSING_ANIMAL_API_CLIENT_EMPTY_RESPONSE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
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

    /** items().item()가 주어진 리스트를 반환하는 정상 페이지 */
    private MissingAnimalApiFullResponse pageWithItems(List<MissingAnimalItemDTO> items) {
        MissingAnimalApiFullResponse full =
                mock(MissingAnimalApiFullResponse.class, Answers.RETURNS_DEEP_STUBS);
        when(full.response().body().items().item()).thenReturn(items);
        return full;
    }

    /** items()는 존재하지만 item()이 null인 '마지막 페이지' 시그널 */
    private MissingAnimalApiFullResponse lastPageNullItem() {
        MissingAnimalApiFullResponse full =
                mock(MissingAnimalApiFullResponse.class, Answers.RETURNS_DEEP_STUBS);
        when(full.response().body().items().item()).thenReturn(null);
        return full;
    }

    /** items() 자체가 null인 비정상 스키마(에러) */
    private MissingAnimalApiFullResponse invalidSchema_itemsNull() {
        MissingAnimalApiFullResponse full =
                mock(MissingAnimalApiFullResponse.class, Answers.RETURNS_DEEP_STUBS);
        when(full.response().body().items()).thenReturn(null);
        return full;
    }

    // ---------------------- 정상 플로우 ----------------------

    @Test
    @DisplayName("단일 페이지 수집 후 다음 페이지가 item()==null → 정상 종료")
    void fetchAll_singlePage_thenNullItem_ends() {
        stubChain();

        var a = mock(MissingAnimalItemDTO.class);
        var b = mock(MissingAnimalItemDTO.class);

        var page1 = pageWithItems(List.of(a, b));
        var page2 = lastPageNullItem(); // 다음 페이지 신호 = 마지막 페이지

        when(responseSpec.body(MissingAnimalApiFullResponse.class)).thenReturn(page1, page2);

        var result = client.fetchAllMissingAnimals("20240101", "20240131");

        assertThat(result).hasSize(2).containsExactly(a, b);
    }

    @Test
    @DisplayName("다중 페이지 수집: p1(2개) → p2(1개) → p3(item()==null)로 종료")
    void fetchAll_multiPages_thenNullItem_ends() {
        stubChain();

        var a = mock(MissingAnimalItemDTO.class);
        var b = mock(MissingAnimalItemDTO.class);
        var c = mock(MissingAnimalItemDTO.class);

        var page1 = pageWithItems(List.of(a, b));
        var page2 = pageWithItems(List.of(c));
        var page3 = lastPageNullItem();

        when(responseSpec.body(MissingAnimalApiFullResponse.class)).thenReturn(page1, page2, page3);

        var result = client.fetchAllMissingAnimals("20240101", "20240131");

        assertThat(result).hasSize(3).containsExactly(a, b, c);
    }

    @Test
    @DisplayName("첫 페이지부터 item()==null → 즉시 종료(빈 결과)")
    void fetchAll_firstPageNullItem_returnsEmpty() {
        stubChain();

        var page1 = lastPageNullItem();
        when(responseSpec.body(MissingAnimalApiFullResponse.class)).thenReturn(page1);

        var result = client.fetchAllMissingAnimals("20240101", "20240131");

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("첫 페이지가 빈 리스트([]) → 마지막 페이지로 간주하고 종료(빈 결과)")
    void fetchAll_firstPageEmptyList_returnsEmpty() {
        stubChain();

        var page1 = pageWithItems(List.of()); // item() == []
        when(responseSpec.body(MissingAnimalApiFullResponse.class)).thenReturn(page1);

        var result = client.fetchAllMissingAnimals("20240101", "20240131");

        assertThat(result).isEmpty();
    }

    // ---------------------- 예외 플로우 ----------------------

    @Test
    @DisplayName("중간 예외: 1페이지 OK 후 2페이지에서 런타임 예외 → CALL_FAILED 래핑")
    void fetchAll_exceptionOnSecondPage_throws() {
        stubChain();

        var i1 = mock(MissingAnimalItemDTO.class);
        var page1 = pageWithItems(List.of(i1));

        when(responseSpec.body(MissingAnimalApiFullResponse.class))
                .thenReturn(page1)                           // 1페이지 OK
                .thenThrow(new RuntimeException("timeout")); // 2페이지 예외

        assertThatThrownBy(() -> client.fetchAllMissingAnimals("20240101", "20240131"))
                .isInstanceOf(MissingAnimalApiClientException.class)
                .hasMessage(MISSING_ANIMAL_API_CLIENT_CALL_FAILED.getValue());
    }

    @Test
    @DisplayName("빈 응답: response()가 null → EMPTY_RESPONSE")
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
    @DisplayName("빈 응답: responseSpec.body(...)가 null → EMPTY_RESPONSE")
    void fetchAll_bodyCallReturnsNull_throws() {
        stubChain();

        when(responseSpec.body(MissingAnimalApiFullResponse.class)).thenReturn(null);

        assertThatThrownBy(() -> client.fetchAllMissingAnimals("20240101", "20240131"))
                .isInstanceOf(MissingAnimalApiClientException.class)
                .hasMessage(MISSING_ANIMAL_API_CLIENT_EMPTY_RESPONSE.getValue());
    }

    @Test
    @DisplayName("빈 응답: items()가 null(스키마 오류) → EMPTY_RESPONSE")
    void fetchAll_itemsNull_throws() {
        stubChain();

        var full = invalidSchema_itemsNull();
        when(responseSpec.body(MissingAnimalApiFullResponse.class)).thenReturn(full);

        assertThatThrownBy(() -> client.fetchAllMissingAnimals("20240101", "20240131"))
                .isInstanceOf(MissingAnimalApiClientException.class)
                .hasMessage(MISSING_ANIMAL_API_CLIENT_EMPTY_RESPONSE.getValue());
    }
}