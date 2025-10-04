package com.kuit.findyou.domain.report.service.sync;

import com.kuit.findyou.domain.image.model.ReportImage;
import com.kuit.findyou.domain.image.repository.ReportImageRepository;
import com.kuit.findyou.domain.report.model.ProtectingReport;
import com.kuit.findyou.domain.report.repository.ProtectingReportRepository;
import com.kuit.findyou.global.common.exception.CustomException;
import com.kuit.findyou.global.external.client.KakaoCoordinateClient;
import com.kuit.findyou.global.external.client.KakaoCoordinateClient.Coordinate;
import com.kuit.findyou.global.external.client.ProtectingAnimalApiClient;
import com.kuit.findyou.global.external.dto.ProtectingAnimalItemDTO;
import com.kuit.findyou.global.external.exception.ProtectingAnimalApiClientException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.*;

import static com.kuit.findyou.global.common.response.status.BaseExceptionResponseStatus.MISSING_REPORT_SYNC_FAILED;
import static com.kuit.findyou.global.common.response.status.BaseExceptionResponseStatus.PROTECTING_REPORT_SYNC_FAILED;
import static com.kuit.findyou.global.external.constant.ExternalExceptionMessage.PROTECTING_ANIMAL_API_CLIENT_CALL_FAILED;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
class ProtectingReportSyncServiceImplTest {

    @Mock ProtectingReportRepository protectingReportRepository;
    @Mock ReportImageRepository reportImageRepository;
    @Mock ProtectingAnimalApiClient protectingAnimalApiClient;
    @Mock KakaoCoordinateClient kakaoCoordinateClient;

    @InjectMocks ProtectingReportSyncServiceImpl service;

    private ProtectingAnimalItemDTO fullItem(
            String noticeNo, String careAddr,
            String pop1, String pop2, String specialMark) {
        ProtectingAnimalItemDTO dto = mock(ProtectingAnimalItemDTO.class);
        when(dto.noticeNo()).thenReturn(noticeNo);
        when(dto.careAddr()).thenReturn(careAddr);
        when(dto.popfile1()).thenReturn(pop1);
        when(dto.popfile2()).thenReturn(pop2);

        // 파서에 들어가는 값들(일반적인 값으로 설정)
        when(dto.kindNm()).thenReturn("진돗개");
        when(dto.upKindNm()).thenReturn("개");
        when(dto.happenDt()).thenReturn("20240718");
        when(dto.sexCd()).thenReturn("M");
        when(dto.age()).thenReturn("3");
        when(dto.weight()).thenReturn("5");
        when(dto.colorCd()).thenReturn("갈색");
        when(dto.neuterYn()).thenReturn("Y");
        when(dto.specialMark()).thenReturn(specialMark);
        when(dto.happenPlace()).thenReturn("서울 강남");
        when(dto.noticeSdt()).thenReturn("20240718");
        when(dto.noticeEdt()).thenReturn("20240725");
        when(dto.careNm()).thenReturn("강남구 보호소");
        when(dto.careTel()).thenReturn("02-000-0000");
        when(dto.orgNm()).thenReturn("강남구청");
        return dto;
    }

    /** 공지번호만 있는, 바로 스킵될 아이템(불필요 스텁 방지) */
    private ProtectingAnimalItemDTO dupItem(String noticeNo) {
        ProtectingAnimalItemDTO dto = mock(ProtectingAnimalItemDTO.class);
        when(dto.noticeNo()).thenReturn(noticeNo);
        return dto;
    }

    @Test
    @DisplayName("신규 1건 추가(이미지 1개), 기존 1건은 유지 → 좌표 1회 호출, 이미지 1건 저장")
    void add_one_new_skip_existing() {
        // given
        ProtectingReport existingStay = mock(ProtectingReport.class);
        when(existingStay.getNoticeNumber()).thenReturn("EXIST-1");
        when(protectingReportRepository.findAll()).thenReturn(List.of(existingStay));

        // API: 기존 공지 + 신규 공지
        ProtectingAnimalItemDTO stay = dupItem("EXIST-1"); // 중복이라 변환 X
        ProtectingAnimalItemDTO n1 = fullItem("NEW-1", "서울 강남", "img1", "", "특이함");
        when(protectingAnimalApiClient.fetchAllProtectingAnimals())
                .thenReturn(List.of(stay, n1));

        when(kakaoCoordinateClient.requestCoordinateOrDefault(anyString()))
                .thenReturn(new Coordinate(new BigDecimal("37.50"), new BigDecimal("127.10")));

        // when
        service.syncProtectingReports();

        // then
        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<ProtectingReport>> reportCap = ArgumentCaptor.forClass(List.class);
        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<ReportImage>> imageCap = ArgumentCaptor.forClass(List.class);

        verify(protectingReportRepository).saveAll(reportCap.capture());
        verify(reportImageRepository).saveAll(imageCap.capture());

        assertThat(reportCap.getValue()).hasSize(1);  // NEW-1만 추가
        assertThat(imageCap.getValue()).hasSize(1);   // pop1만 존재

        verify(kakaoCoordinateClient, times(1)).requestCoordinateOrDefault(eq("서울 강남"));

        // 삭제는 없음(둘 다 API에 존재하므로)
        verify(protectingReportRepository)
                .deleteAll(argThat((Iterable<ProtectingReport> it) -> it != null && !it.iterator().hasNext()));
    }

    @Test
    @DisplayName("API에 없는 기존 공지(B)를 삭제, 추가는 없음 → 좌표 호출 0회, 저장 0건")
    void delete_obsolete_no_add() {
        // given: 기존 A(유지), B(삭제대상)
        ProtectingReport stay = mock(ProtectingReport.class);
        when(stay.getNoticeNumber()).thenReturn("A");
        ProtectingReport obsolete = mock(ProtectingReport.class);
        when(obsolete.getNoticeNumber()).thenReturn("B");
        when(protectingReportRepository.findAll()).thenReturn(List.of(stay, obsolete));

        // API: A만 존재 → B는 삭제
        ProtectingAnimalItemDTO apiA = dupItem("A");
        when(protectingAnimalApiClient.fetchAllProtectingAnimals())
                .thenReturn(List.of(apiA));

        // when
        service.syncProtectingReports();

        // then
        // B만 삭제 대상
        ArgumentCaptor<Iterable<ProtectingReport>> delCap = ArgumentCaptor.forClass(Iterable.class);
        verify(protectingReportRepository).deleteAll(delCap.capture());

        Iterable<ProtectingReport> captured = delCap.getValue();
        Iterator<ProtectingReport> iter = captured.iterator();

        assertThat(iter.hasNext()).isTrue();
        ProtectingReport only = iter.next();
        assertThat(only).isSameAs(obsolete);
        assertThat(iter.hasNext()).isFalse();

        // 추가 없음 → saveAll 둘 다 빈 리스트
        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<ProtectingReport>> reportCap = ArgumentCaptor.forClass(List.class);
        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<ReportImage>> imageCap = ArgumentCaptor.forClass(List.class);

        verify(protectingReportRepository).saveAll(reportCap.capture());
        verify(reportImageRepository).saveAll(imageCap.capture());

        assertThat(reportCap.getValue()).isEmpty();
        assertThat(imageCap.getValue()).isEmpty();

        verifyNoInteractions(kakaoCoordinateClient);
    }

    @Test
    @DisplayName("특이사항이 null이면 '미등록'으로 저장, 이미지 2장 저장")
    void specialMark_null_defaults_and_two_images_saved() {
        // given: 기존 없음
        when(protectingReportRepository.findAll()).thenReturn(List.of());

        // API: 신규 1건, 특이사항 null, 이미지 2개
        ProtectingAnimalItemDTO n1 = fullItem("N-1", "서울 송파", "i1", "i2", null);
        when(protectingAnimalApiClient.fetchAllProtectingAnimals())
                .thenReturn(List.of(n1));

        when(kakaoCoordinateClient.requestCoordinateOrDefault(anyString()))
                .thenReturn(new Coordinate(new BigDecimal("37.48"), new BigDecimal("127.12")));

        // when
        service.syncProtectingReports();

        // then
        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<ProtectingReport>> reportCap = ArgumentCaptor.forClass(List.class);
        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<ReportImage>> imageCap = ArgumentCaptor.forClass(List.class);

        verify(protectingReportRepository).saveAll(reportCap.capture());
        verify(reportImageRepository).saveAll(imageCap.capture());

        assertThat(reportCap.getValue()).hasSize(1);
        ProtectingReport saved = reportCap.getValue().get(0);

        assertThat(saved.getSignificant()).isEqualTo("미등록");
        assertThat(imageCap.getValue()).hasSize(2);
        verify(kakaoCoordinateClient, times(1)).requestCoordinateOrDefault(eq("서울 송파"));

        // 삭제는 없음
        verify(protectingReportRepository)
                .deleteAll(argThat((Iterable<ProtectingReport> it) -> it != null && !it.iterator().hasNext()));

    }

    @Test
    @DisplayName("API가 빈 리스트면 기존 모든 공지 삭제, 추가/좌표호출 없음")
    void api_empty_deletes_all_existing() {
        // given: 기존 2건 존재
        ProtectingReport r1 = mock(ProtectingReport.class);
        when(r1.getNoticeNumber()).thenReturn("X");
        ProtectingReport r2 = mock(ProtectingReport.class);
        when(r2.getNoticeNumber()).thenReturn("Y");
        when(protectingReportRepository.findAll()).thenReturn(List.of(r1, r2));

        // API는 빈 리스트
        when(protectingAnimalApiClient.fetchAllProtectingAnimals()).thenReturn(List.of());

        // when
        service.syncProtectingReports();

        // then: 둘 다 삭제 대상
        @SuppressWarnings("unchecked")
        ArgumentCaptor<Iterable<ProtectingReport>> delCap = ArgumentCaptor.forClass(Iterable.class);
        verify(protectingReportRepository).deleteAll(delCap.capture());

        List<ProtectingReport> deleted = new ArrayList<>();
        delCap.getValue().forEach(deleted::add);
        assertThat(deleted).containsExactlyInAnyOrder(r1, r2);

        // 추가 없음
        verify(protectingReportRepository).saveAll(eq(Collections.emptyList()));
        verify(reportImageRepository).saveAll(eq(Collections.emptyList()));

        // 좌표 변환도 호출되지 않음
        verifyNoInteractions(kakaoCoordinateClient);
    }

    @Test
    @DisplayName("이미지 둘 다 null/blank → 이미지 0장 저장 브랜치")
    void no_images_saved_when_both_blank() {
        when(protectingReportRepository.findAll()).thenReturn(List.of());

        // pop1=null, pop2="  " (isBlank true)
        ProtectingAnimalItemDTO n1 = fullItem("N-2", "서울 마포", null, "  ", "메모있음");
        when(protectingAnimalApiClient.fetchAllProtectingAnimals()).thenReturn(List.of(n1));

        when(kakaoCoordinateClient.requestCoordinateOrDefault(anyString()))
                .thenReturn(new Coordinate(new BigDecimal("37.55"), new BigDecimal("126.91")));

        service.syncProtectingReports();

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<ReportImage>> imageCap = ArgumentCaptor.forClass(List.class);
        verify(reportImageRepository).saveAll(imageCap.capture());

        assertThat(imageCap.getValue()).isEmpty();           // 0장
        verify(kakaoCoordinateClient).requestCoordinateOrDefault("서울 마포");
    }

    @Test
    @DisplayName("특이사항이 채워져 있으면 그대로 저장 (기본값 미사용)")
    void specialMark_pass_through_when_present() {
        when(protectingReportRepository.findAll()).thenReturn(List.of());

        ProtectingAnimalItemDTO n1 = fullItem("N-3", "서울 영등포", "p1", null, "현관 앞에서 구조");
        when(protectingAnimalApiClient.fetchAllProtectingAnimals()).thenReturn(List.of(n1));

        when(kakaoCoordinateClient.requestCoordinateOrDefault(anyString()))
                .thenReturn(new Coordinate(new BigDecimal("37.52"), new BigDecimal("126.90")));

        service.syncProtectingReports();

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<ProtectingReport>> reportCap = ArgumentCaptor.forClass(List.class);
        verify(protectingReportRepository).saveAll(reportCap.capture());

        ProtectingReport saved = reportCap.getValue().get(0);
        assertThat(saved.getSignificant()).isEqualTo("현관 앞에서 구조"); // 기본값 아님
    }

    @Test
    @DisplayName("API 예외 발생 시: 예외 전파되고, 저장/삭제/좌표 모두 호출 안 됨")
    void api_throw_is_propagated_and_no_side_effects() {
        // findAll 조차 안 불리게(= synchronizeData 진입 전) API에서 바로 예외
        when(protectingAnimalApiClient.fetchAllProtectingAnimals())
                .thenThrow(new ProtectingAnimalApiClientException(PROTECTING_ANIMAL_API_CLIENT_CALL_FAILED));

        // 실행 시 예외가 전파되어야 함
        assertThatThrownBy(() -> service.syncProtectingReports())
                .isInstanceOf(CustomException.class)
                .hasMessage(PROTECTING_REPORT_SYNC_FAILED.getMessage());

        // 부수효과 없음 검증
        verifyNoInteractions(protectingReportRepository, reportImageRepository, kakaoCoordinateClient);
    }

}
