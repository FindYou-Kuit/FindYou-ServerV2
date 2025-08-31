package com.kuit.findyou.domain.inquiry.service;

import com.kuit.findyou.domain.inquiry.dto.AddInquiryRequest;

public interface InquiryService {
    void addInquiry(Long userId, AddInquiryRequest request);
}
