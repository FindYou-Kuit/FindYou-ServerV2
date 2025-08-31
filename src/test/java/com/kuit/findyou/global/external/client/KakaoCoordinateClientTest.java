package com.kuit.findyou.global.external.client;

import com.kuit.findyou.global.external.dto.KakaoAddressResponse;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriBuilder;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.web.util.UriUtils;

import java.math.BigDecimal;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ActiveProfiles("test")
@ExtendWith(org.mockito.junit.jupiter.MockitoExtension.class)
class KakaoCoordinateClientTest {

    @Mock RestClient restClient;
    @Mock KakaoCoordinateClient self; // fallback 테스트용
    @Mock RestClient.RequestHeadersUriSpec<?> uriSpec;
    @Mock RestClient.ResponseSpec responseSpec;

    KakaoCoordinateClient client;

    @BeforeEach
    void setUp() {
        client = new KakaoCoordinateClient(restClient, self);
    }

    @Test
    @DisplayName("성공: documents[0]의 (x=경도, y=위도) → (longitude, latitude) 매핑")
    void requestCoordinateFromKakaoApi_success() {
        // 체인 모킹
        when(restClient.get()).thenReturn((RestClient.RequestHeadersUriSpec) uriSpec);
        when(uriSpec.uri(any(Function.class))).thenReturn(uriSpec);
        when(uriSpec.retrieve()).thenReturn(responseSpec);

        // 응답 객체 (meta 포함!)
        KakaoAddressResponse.Document doc = new KakaoAddressResponse.Document("127.100", "37.500");
        KakaoAddressResponse.Meta meta = new KakaoAddressResponse.Meta(1, 1, true);
        KakaoAddressResponse kakao = new KakaoAddressResponse(List.of(doc), meta);
        when(responseSpec.body(KakaoAddressResponse.class)).thenReturn(kakao);

        KakaoCoordinateClient.Coordinate coord = client.requestCoordinateFromKakaoApi("서울");

        assertThat(coord.latitude()).isEqualByComparingTo(new BigDecimal("37.500"));  // y -> latitude
        assertThat(coord.longitude()).isEqualByComparingTo(new BigDecimal("127.100")); // x -> longitude

        // query 파라미터 설정 함수가 호출되었는지 확인 (원하면 내용도 검증)
        ArgumentCaptor<Function<UriBuilder, URI>> captor = ArgumentCaptor.forClass(Function.class);
        verify(uriSpec).uri(captor.capture());
        URI built = captor.getValue().apply(UriComponentsBuilder.fromHttpUrl("http://localhost"));

        String raw = UriComponentsBuilder.fromUri(built).build()
                .getQueryParams().getFirst("query");
        assertThat(URLDecoder.decode(raw, StandardCharsets.UTF_8)).isEqualTo("서울");
    }

    @Test
    @DisplayName("documents가 빈 배열 → 기본 좌표(0.0, 0.0) 반환")
    void requestCoordinateFromKakaoApi_emptyDocuments_returnsDefault() {
        when(restClient.get()).thenReturn((RestClient.RequestHeadersUriSpec) uriSpec);
        when(uriSpec.uri(any(Function.class))).thenReturn(uriSpec);
        when(uriSpec.retrieve()).thenReturn(responseSpec);

        KakaoAddressResponse kakao = new KakaoAddressResponse(List.of(), new KakaoAddressResponse.Meta(0, 0, true));
        when(responseSpec.body(KakaoAddressResponse.class)).thenReturn(kakao);

        KakaoCoordinateClient.Coordinate coord = client.requestCoordinateFromKakaoApi("부산");

        assertThat(coord.latitude()).isEqualByComparingTo("0.0");
        assertThat(coord.longitude()).isEqualByComparingTo("0.0");
    }

    @Test
    @DisplayName("응답 body가 null → 기본 좌표 반환")
    void requestCoordinateFromKakaoApi_nullBody_returnsDefault() {
        when(restClient.get()).thenReturn((RestClient.RequestHeadersUriSpec) uriSpec);
        when(uriSpec.uri(any(Function.class))).thenReturn(uriSpec);
        when(uriSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.body(KakaoAddressResponse.class)).thenReturn(null); // null body

        KakaoCoordinateClient.Coordinate coord = client.requestCoordinateFromKakaoApi("대구");

        assertThat(coord.latitude()).isEqualByComparingTo("0.0");
        assertThat(coord.longitude()).isEqualByComparingTo("0.0");
    }

    @Test
    @DisplayName("fallback: self.requestCoordinateFromKakaoApi 예외 → 기본 좌표 반환")
    void requestCoordinateOrDefault_whenSelfThrows_returnsDefault() {
        when(self.requestCoordinateFromKakaoApi("서울 강남"))
                .thenThrow(new RuntimeException("timeout"));

        KakaoCoordinateClient.Coordinate coord = client.requestCoordinateOrDefault("서울 강남");

        assertThat(coord.latitude()).isEqualByComparingTo("0.0");
        assertThat(coord.longitude()).isEqualByComparingTo("0.0");
        verify(self).requestCoordinateFromKakaoApi("서울 강남");
    }
}
