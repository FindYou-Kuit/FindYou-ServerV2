package com.kuit.findyou.domain.report.service.detail;

import com.kuit.findyou.domain.report.model.Report;
import com.kuit.findyou.global.common.external.client.KakaoAddressClient;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@RequiredArgsConstructor
@Service
public class CoordinateUpdateService {

    private final KakaoAddressClient kakaoAddressClient;

    @PersistenceContext
    private EntityManager em;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void updateCoordinates(Long reportId, String address) {
        try {
            Report report = em.find(Report.class, reportId);
            if (report != null && report.isCoordinatesAbsent()) {
                KakaoAddressClient.Coordinate coordinate =
                        kakaoAddressClient.getCoordinatesFromAddress(address);

                report.setLatitude(coordinate.latitude());
                report.setLongitude(coordinate.longitude());

                log.info("좌표 갱신: reportId={}", reportId);
            }
        } catch (Exception e) {
            log.warn("좌표 갱신 실패: reportId={}", reportId);
        }
    }
}
