package com.kuit.findyou.domain.inquiry.dto;

import java.util.List;

public record AddInquiryRequest(
        String title,
        String content,
        List<String> categories
) {
}
