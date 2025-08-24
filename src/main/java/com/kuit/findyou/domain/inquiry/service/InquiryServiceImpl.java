package com.kuit.findyou.domain.inquiry.service;

import com.kuit.findyou.domain.inquiry.dto.AddInquiryRequest;
import com.kuit.findyou.domain.inquiry.model.Inquiry;
import com.kuit.findyou.domain.inquiry.repository.InquiryRepository;
import com.kuit.findyou.domain.user.model.User;
import com.kuit.findyou.domain.user.repository.UserRepository;
import com.kuit.findyou.global.common.exception.CustomException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import static com.kuit.findyou.global.common.response.status.BaseExceptionResponseStatus.BAD_REQUEST;

@Slf4j
@RequiredArgsConstructor
@Service
public class InquiryServiceImpl implements InquiryService {
    private final UserRepository userRepository;
    private final InquiryRepository inquiryRepository;

    @Transactional
    @Override
    public void addInquiry(Long userId, AddInquiryRequest request) {
        log.info("[addInquiry] userId = {} title = {}", userId, request.title());

        User user = userRepository.getReferenceById(userId);

        String category = String.join(",", request.categories());

        Inquiry inquiry = Inquiry.builder()
                .category(category)
                .title(request.title())
                .content(request.content())
                .user(user)
                .build();

        inquiryRepository.save(inquiry);
    }
}
