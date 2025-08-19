package com.kuit.findyou.domain.information.util;

import com.kuit.findyou.domain.information.dto.UpdateVolunteerWorkRequest;
import com.kuit.findyou.domain.information.model.VolunteerWork;
import com.kuit.findyou.global.external.dto.VolunteerWorksByKeywordApiResponse;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;

@Component
public class VolunteerWorksByKeywordApiResponseUtil {
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyyMMdd");
    private final DateTimeFormatter timeFormatter = new DateTimeFormatterBuilder()
            .appendValue(ChronoField.HOUR_OF_DAY, 1, 2, java.time.format.SignStyle.NOT_NEGATIVE)
            .toFormatter();
    private final String RECRUITING_STATUS = "2";

    public UpdateVolunteerWorkRequest convertItemIntoUpdateRequest(VolunteerWorksByKeywordApiResponse.Item item, long runId){
        return new UpdateVolunteerWorkRequest(item.getNanmmbyNm().trim(),
                LocalDate.parse(item.getNoticeBgnde().trim(), dateFormatter),
                LocalDate.parse(item.getNoticeEndde().trim(), dateFormatter),
                item.getActPlace().trim(),
                LocalDate.parse(item.getProgrmBgnde().trim(), dateFormatter).atTime(LocalTime.parse(item.getActBeginTm().trim(), timeFormatter)),
                LocalDate.parse(item.getProgrmEndde().trim(), dateFormatter).atTime(LocalTime.parse(item.getActEndTm().trim(), timeFormatter)),
                item.getUrl().trim(),
                item.getProgrmRegistNo().trim(),
                runId
        );
    }

    public boolean isLastPage(VolunteerWorksByKeywordApiResponse response) {
        int totalCount = Integer.parseInt(response.getBody().getTotalCount().trim());
        int numOfRows = Integer.parseInt(response.getBody().getNumOfRows().trim());
        int pageNo = Integer.parseInt(response.getBody().getPageNo().trim());

        if(numOfRows <= 0) return true;

        int lastPageNo = (int) Math.ceil((double) totalCount / numOfRows );
        return pageNo >= lastPageNo;
    }

    public boolean isRecruiting(VolunteerWorksByKeywordApiResponse.Item item){
        return item.getProgrmSttusSe().trim().equals(RECRUITING_STATUS);
    }

    public VolunteerWork convertItemIntoEntity(VolunteerWorksByKeywordApiResponse.Item item, long runId) {
        return VolunteerWork.builder()
                .institution(item.getNanmmbyNm())
                .recruitmentStartDate(LocalDate.parse(item.getNoticeBgnde().trim(), dateFormatter))
                .recruitmentEndDate(LocalDate.parse(item.getNoticeEndde().trim(), dateFormatter))
                .address(item.getActPlace().trim())
                .volunteerStartAt(
                        LocalDate.parse(item.getProgrmBgnde().trim(), dateFormatter).atTime(LocalTime.parse(item.getActBeginTm().trim(), timeFormatter))
                )
                .volunteerEndAt(
                        LocalDate.parse(item.getProgrmEndde().trim(), dateFormatter).atTime(LocalTime.parse(item.getActEndTm().trim(), timeFormatter))
                )
                .webLink(item.getUrl().trim())
                .registerNumber(item.getProgrmRegistNo().trim())
                .runId(runId)
                .build();
    }
}
