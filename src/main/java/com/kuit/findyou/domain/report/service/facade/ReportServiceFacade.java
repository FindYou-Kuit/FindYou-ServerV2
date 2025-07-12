package com.kuit.findyou.domain.report.service.facade;

import com.kuit.findyou.domain.report.model.ReportTag;
import com.kuit.findyou.domain.report.service.detail.ReportDetailService;
import com.kuit.findyou.domain.user.repository.UserRepository;
import com.kuit.findyou.global.common.exception.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import static com.kuit.findyou.global.common.response.status.BaseExceptionResponseStatus.*;

@Service
@RequiredArgsConstructor
public class ReportServiceFacade {

    private final ReportDetailService reportDetailService;
    private final UserRepository userRepository;

    public <DTO_TYPE> DTO_TYPE getReportDetail(ReportTag tag, Long reportId, Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new CustomException(USER_NOT_FOUND);
        }

        return reportDetailService.getReportDetail(tag, reportId, userId);
    }
}


