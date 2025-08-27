package com.kuit.findyou.domain.information.service;

import com.kuit.findyou.domain.information.dto.UpdateVolunteerWorkRequest;
import com.kuit.findyou.domain.information.model.VolunteerWork;
import com.kuit.findyou.domain.information.repository.VolunteerWorkRepository;
import com.kuit.findyou.domain.information.service.animalShelter.SyncVolunteerWorkServiceImpl;
import com.kuit.findyou.domain.information.util.VolunteerWorksByKeywordApiResponseUtil;
import com.kuit.findyou.global.external.client.VolunteerWorkApiClient;
import com.kuit.findyou.global.external.dto.VolunteerWorksByKeywordApiResponse;
import com.kuit.findyou.global.external.exception.VolunteerWorkApiException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SyncVolunteerWorkServiceTest {
    @InjectMocks
    SyncVolunteerWorkServiceImpl service;
    @Mock
    VolunteerWorkApiClient apiClient;
    @Mock
    VolunteerWorksByKeywordApiResponseUtil util;
    @Mock
    VolunteerWorkRepository volunteerWorkRepository;

    private VolunteerWorksByKeywordApiResponse apiResponse() {
        return mock(VolunteerWorksByKeywordApiResponse.class, RETURNS_DEEP_STUBS);
    }
    private VolunteerWorksByKeywordApiResponse.Item item() {
        return mock(VolunteerWorksByKeywordApiResponse.Item.class);
    }

    private VolunteerWork work(){
        return mock(VolunteerWork.class);
    }

    private UpdateVolunteerWorkRequest updateRequest(){
        return mock(UpdateVolunteerWorkRequest.class);
    }
    @DisplayName("외부 API에 문제가 없다면 동기화는 성공한다")
    @Test
    void should_SynchronizingSucceed_When_ExternalApiIsFine() {
        // given
        var keyword1page1 = apiResponse();
        var keyword1page2 = apiResponse();
        var keyword2page1 = apiResponse();
        var keyword2page2 = apiResponse();
        var keyword3page1 = apiResponse();
        var keyword3page2 = apiResponse();

        when(apiClient.getVolunteerWorksByKeyword(anyInt(), eq("유기동물"), anyInt()))
                .thenReturn(keyword1page1, keyword1page2);

        when(apiClient.getVolunteerWorksByKeyword(anyInt(), eq("유기견"), anyInt()))
                .thenReturn(keyword2page1, keyword2page2);

        when(apiClient.getVolunteerWorksByKeyword(anyInt(), eq("유기묘"), anyInt()))
                .thenReturn(keyword3page1, keyword3page2);

        var item1 = item(); // 맨 앞 두개만 모집 중
        when(item1.getProgrmRegistNo()).thenReturn("number" + 1);
        var item2 = item();
        when(item2.getProgrmRegistNo()).thenReturn("number" + 2);
        var item3 = item();

        List<VolunteerWorksByKeywordApiResponse.Item> pageItems = List.of(item1, item2, item3);
        when(keyword1page1.getBody().getItems()).thenReturn(pageItems);
        when(keyword1page2.getBody().getItems()).thenReturn(pageItems);
        when(keyword2page1.getBody().getItems()).thenReturn(pageItems);
        when(keyword2page2.getBody().getItems()).thenReturn(pageItems);
        when(keyword3page1.getBody().getItems()).thenReturn(pageItems);
        when(keyword3page2.getBody().getItems()).thenReturn(pageItems);

        when(util.isRecruiting(any())).thenReturn(
                true, true, false,
                true, true, false,
                true, true, false,
                true, true, false,
                true, true, false,
                true, true, false
        );

        VolunteerWork mockWork = work();
        when(mockWork.getRegisterNumber()).thenReturn("number" + 1);

        List<VolunteerWork> works = List.of(mockWork);
        when(volunteerWorkRepository.findAllByRegisterNumberIn(anyList())).thenReturn(works); // 기존 데이터는 맨 앞 하나

        when(util.convertItemIntoEntity(any(), anyLong())).thenReturn(work());

        when(util.convertItemIntoUpdateRequest(any(), anyLong())).thenReturn(updateRequest());

        when(util.isLastPage(keyword1page1)).thenReturn(false);
        when(util.isLastPage(keyword1page2)).thenReturn(true);
        when(util.isLastPage(keyword2page1)).thenReturn(false);
        when(util.isLastPage(keyword2page2)).thenReturn(true);
        when(util.isLastPage(keyword3page1)).thenReturn(false);
        when(util.isLastPage(keyword3page2)).thenReturn(true);

        // when
        service.synchronize();

        // then
        verify(volunteerWorkRepository, times(6)).findAllByRegisterNumberIn(anyList());
        verify(volunteerWorkRepository, times(6)).saveAll(anyList());
        verify(volunteerWorkRepository, times(6)).flush();

        verify(util, times(6)).convertItemIntoEntity(any(), anyLong());
        verify(util, times(6)).convertItemIntoUpdateRequest(any(), anyLong());

        verify(volunteerWorkRepository, times(1)).deleteAllByRunIdNot(anyLong());
    }

    @DisplayName("한 키워드에 대해서 파싱이 실패해도 나머지는 계속 수행된다.")
    @Test
    void should_ContinueSynchronizing_When_OneKeywordFailed(){
        // given
        var okResp = apiResponse();
        when(apiClient.getVolunteerWorksByKeyword(anyInt(), eq("유기동물"), anyInt()))
                .thenReturn(okResp);
        when(util.isLastPage(okResp)).thenReturn(true);
        when(okResp.getBody().getItems()).thenReturn(List.of());

        when(apiClient.getVolunteerWorksByKeyword(anyInt(), eq("유기견"), anyInt()))
                .thenThrow(new VolunteerWorkApiException("두번째 키워드에서 예외 발생"));

        var okResp3 = apiResponse();
        when(apiClient.getVolunteerWorksByKeyword(anyInt(), eq("유기묘"), anyInt()))
                .thenReturn(okResp3);
        when(util.isLastPage(okResp3)).thenReturn(true);
        when(okResp3.getBody().getItems()).thenReturn(List.of());

        // when
        service.synchronize();

        // then
        verify(volunteerWorkRepository, times(2)).findAllByRegisterNumberIn(anyList());

        verify(volunteerWorkRepository, times(1)).deleteAllByRunIdNot(anyLong());
    }
}