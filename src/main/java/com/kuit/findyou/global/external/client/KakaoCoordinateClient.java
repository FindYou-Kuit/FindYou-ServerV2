package com.kuit.findyou.global.external.client;

import com.kuit.findyou.global.external.dto.KakaoAddressResponse;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;

@Component
@Slf4j
public class KakaoCoordinateClient {

    private final RestClient kakaoAddressRestClient;
    private final KakaoCoordinateClient self;

    private static final Coordinate DEFAULT_COORDINATE = new Coordinate(BigDecimal.valueOf(0.0), BigDecimal.valueOf(0.0));

    public KakaoCoordinateClient(
            @Qualifier("kakaoAddressRestClient") RestClient kakaoAddressRestClient,
            @Lazy KakaoCoordinateClient self) {
        this.kakaoAddressRestClient = kakaoAddressRestClient;
        this.self = self;
    }

    public Coordinate requestCoordinateOrDefault(String address) {
        try {
            return self.requestCoordinateFromKakaoApi(address); // retry + rateLimit
        } catch (Exception e) {
            log.error("[카카오 좌표 API 3회 재시도 실패 - fallback 적용] address={}", address, e);
            return DEFAULT_COORDINATE;
        }
    }

    @Retry(name = "kakao")
    @RateLimiter(name = "kakao")
    public Coordinate requestCoordinateFromKakaoApi(String address) {
        KakaoAddressResponse response = kakaoAddressRestClient.get()
                .uri(uriBuilder -> uriBuilder.queryParam("query", address).build())
                .retrieve()
                .body(KakaoAddressResponse.class);

        if (response == null || response.documents().isEmpty()) {
            log.warn("[Kakao 주소 변환 실패] address={}", address);
            return DEFAULT_COORDINATE;
        }

        KakaoAddressResponse.Document document = response.documents().get(0);
        return new Coordinate(new BigDecimal(document.y()), new BigDecimal(document.x()));
    }


    public record Coordinate(BigDecimal latitude, BigDecimal longitude) {}
}
