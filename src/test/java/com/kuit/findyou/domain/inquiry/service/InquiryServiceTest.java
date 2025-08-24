package com.kuit.findyou.domain.inquiry.service;

import com.kuit.findyou.domain.inquiry.dto.AddInquiryRequest;
import com.kuit.findyou.domain.inquiry.repository.InquiryRepository;
import com.kuit.findyou.domain.user.model.User;
import com.kuit.findyou.domain.user.repository.UserRepository;
import com.kuit.findyou.global.common.exception.CustomException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static com.kuit.findyou.global.common.response.status.BaseExceptionResponseStatus.BAD_REQUEST;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InquiryServiceTest {
    @InjectMocks
    InquiryServiceImpl inquiryService;
    @Mock
    UserRepository userRepository;
    @Mock
    InquiryRepository inquiryRepository;

    @DisplayName("여러 카테고리와 함께 문의사항을 추가하면 저장된다")
    @Test
    void shouldSaveInquiry_WhenAddingWithCategories(){
        // given
        final long userId = 1L;
        AddInquiryRequest request = mock(AddInquiryRequest.class);
        User user = mock(User.class);
        when(request.title()).thenReturn("title");
        when(request.content()).thenReturn("content");
        when(request.categories()).thenReturn(List.of("cat1", "cat2"));
        when(userRepository.getReferenceById(anyLong())).thenReturn(user);

        // when
        inquiryService.addInquiry(userId, request);

        // then
        verify(inquiryRepository).save(argThat(inquiry ->
                inquiry.getCategory().equals("cat1&cat2") &&
                        inquiry.getTitle().equals("title") &&
                        inquiry.getContent().equals("content") &&
                        inquiry.getUser().equals(user)
        ));

        ArgumentCaptor<Long> longCaptor = ArgumentCaptor.forClass(Long.class);
        verify(userRepository).getReferenceById(longCaptor.capture());
        assertThat(longCaptor.getValue()).isEqualTo(userId);

        verify(inquiryRepository).save(any());
    }

    @DisplayName("한 카테고리와 함께 문의사항을 추가하면 저장된다")
    @Test
    void shouldSaveInquiry_WhenAddingWithOneCategory(){
        // given
        final long userId = 1L;
        User user = mock(User.class);
        when(userRepository.getReferenceById(anyLong())).thenReturn(user);

        AddInquiryRequest request = mock(AddInquiryRequest.class);
        when(request.title()).thenReturn("title");
        when(request.content()).thenReturn("content");
        when(request.categories()).thenReturn(List.of("cat1"));

        // when
        inquiryService.addInquiry(userId, request);

        // then
        verify(inquiryRepository).save(argThat(inquiry ->
                inquiry.getCategory().equals("cat1") &&
                        inquiry.getTitle().equals("title") &&
                        inquiry.getContent().equals("content") &&
                        inquiry.getUser().equals(user)
        ));

        ArgumentCaptor<Long> longCaptor = ArgumentCaptor.forClass(Long.class);
        verify(userRepository).getReferenceById(longCaptor.capture());
        assertThat(longCaptor.getValue()).isEqualTo(userId);

        verify(inquiryRepository).save(any());
    }
}