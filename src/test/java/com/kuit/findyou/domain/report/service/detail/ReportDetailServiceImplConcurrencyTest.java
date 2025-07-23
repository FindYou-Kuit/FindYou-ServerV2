package com.kuit.findyou.domain.report.service.detail;

import com.kuit.findyou.domain.report.dto.response.ProtectingReportDetailResponseDTO;
import com.kuit.findyou.domain.report.model.ProtectingReport;
import com.kuit.findyou.domain.report.repository.ProtectingReportRepository;
import com.kuit.findyou.domain.report.repository.InterestReportRepository;
import com.kuit.findyou.domain.user.model.Role;
import com.kuit.findyou.domain.user.model.User;
import com.kuit.findyou.domain.user.repository.UserRepository;
import jakarta.persistence.EntityManager;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import com.kuit.findyou.domain.report.model.*;
import com.kuit.findyou.domain.report.strategy.ReportDetailStrategy;
import com.kuit.findyou.global.common.external.client.KakaoCoordinateClient;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@Slf4j
@SpringBootTest
@ActiveProfiles("test")
public class ReportDetailServiceImplConcurrencyTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProtectingReportRepository reportRepository;

    @Autowired
    private EntityManager em;

    @Autowired
    private PlatformTransactionManager transactionManager;

    @MockitoBean
    private KakaoCoordinateClient kakaoCoordinateClient;

    @Autowired
    private InterestReportRepository interestReportRepository;

    @Autowired
    private ReportDetailServiceImpl reportDetailService;

    private Long userId;
    private Long reportId;

    @BeforeEach
    void setUp() {
        // TransactionTemplate을 사용하여 테스트 데이터를 별도 트랜잭션으로 커밋
        TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);

        transactionTemplate.execute(status -> {
            // 사용자 삽입
            User user = userRepository.save(User.builder()
                    .name("홍길동")
                    .profileImageUrl("http://example.com/profile.png")
                    .kakaoId(123131231231L)
                    .role(Role.USER)
                    .build());

            userId = user.getId();

            // 좌표가 없는 ProtectingReport 삽입
            ProtectingReport report = ProtectingReport.createProtectingReport(
                    "페르시안", "고양이", ReportTag.PROTECTING, LocalDate.now().minusDays(1),
                    "서울시 마포구 월드컵북로 212", user, Sex.F, "2살", "4kg", "흰색", Neutering.Y,
                    "왼쪽 귀에 상처", "마포대교 근처", "NOTICE-2024-001",
                    LocalDate.now(), LocalDate.now().plusDays(14), "마포구 동물보호센터", "02-123-4567", "마포구청",
                    null, null
            );

            reportId = reportRepository.save(report).getId();

            // 강제로 플러시하여 DB에 반영
            em.flush();

            return null;
        });

        em.clear();
    }

    @DisplayName("동시성 좌표갱신 시 두 응답 모두 좌표 포함 여부 확인")
    @Test
    void getCoordinatesFromAddressConcurrencyResponseTest() throws InterruptedException {
        // 외부 API mock
        when(kakaoCoordinateClient.getCoordinatesFromAddress(any()))
                .thenReturn(new KakaoCoordinateClient.Coordinate(
                        new BigDecimal("37.123"), new BigDecimal("127.456"))
                );

        CountDownLatch latch = new CountDownLatch(2);

        AtomicReference<ProtectingReportDetailResponseDTO> response1 = new AtomicReference<>();
        AtomicReference<ProtectingReportDetailResponseDTO> response2 = new AtomicReference<>();

        Runnable task1 = () -> {
            log.info("Thread1 시작 - reportId: {}, userId: {}", reportId, userId);
            ProtectingReportDetailResponseDTO result = reportDetailService.getReportDetail(ReportTag.PROTECTING, reportId, userId);
            response1.set(result);
            latch.countDown();
        };

        Runnable task2 = () -> {
            log.info("Thread2 시작 - reportId: {}, userId: {}", reportId, userId);
            ProtectingReportDetailResponseDTO result = reportDetailService.getReportDetail(ReportTag.PROTECTING, reportId, userId);
            response2.set(result);
            latch.countDown();
        };

        Thread thread1 = new Thread(task1);
        Thread thread2 = new Thread(task2);

        thread1.start();
        thread2.start();

        latch.await();

        // then: 두 응답 모두 좌표가 포함되어야 함
        assertThat(response1.get().latitude()).isNotNull();
        assertThat(response1.get().longitude()).isNotNull();

        assertThat(response2.get().latitude()).isNotNull();
        assertThat(response2.get().longitude()).isNotNull();

        log.info("테스트 완료 - 두 응답 모두 좌표 포함됨");
    }

}