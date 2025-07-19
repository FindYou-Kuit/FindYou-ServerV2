package com.kuit.findyou.global.common.external.dto;

import java.util.List;

public record KakaoAddressResponse(
        List<Document> documents,
        Meta meta
) {
    public record Document(
            String x,  // 경도
            String y   // 위도
    ) {}

    public record Meta(
            int total_count,
            int pageable_count,
            boolean is_end
    ) {}
}


