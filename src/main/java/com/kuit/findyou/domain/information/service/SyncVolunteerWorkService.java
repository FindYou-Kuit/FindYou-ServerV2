package com.kuit.findyou.domain.information.service;

import com.kuit.findyou.domain.information.model.VolunteerWork;
import com.kuit.findyou.domain.information.repository.VolunteerWorkRepository;
import com.kuit.findyou.domain.information.util.VolunteerWorksByKeywordApiResponseUtil;
import com.kuit.findyou.global.external.client.VolunteerWorkApiClient;
import com.kuit.findyou.global.external.dto.VolunteerWorksByKeywordApiResponse;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Service
public class SyncVolunteerWorkService {
    private final VolunteerWorkApiClient volunteerWorkApiClient;
    private final VolunteerWorkRepository volunteerWorkRespository;
    private final VolunteerWorksByKeywordApiResponseUtil responseUtil;
    private final List<String> keywords = List.of("유기동물", "유기견", "유기묘");
    private final int NUM_OF_ROWS = 50;
    @Transactional
    public void synchronize(){
        // 키워드 별로 봉사 동기화
        log.info("[synchronize] 봉사활동 정보 동기화 시작");

        long runId = System.currentTimeMillis();

        for(String keyword : keywords){
            try {
                fetchDataContainingKeyword(keyword, runId);
                log.info("[synchronize] 키워드 '{}' 동기화 완료", keyword);
            } catch (Exception e) {
                log.error("[synchronize] 키워드 '{}' 동기화 실패: {}", keyword, e.toString(), e);
            }
        }

        // 모집 상태가 아닌 데이터는 삭제
        volunteerWorkRespository.deleteAllByRunIdNot(runId);
        log.info("[synchronize] 모집 마감 봉사활동 삭제 완료");
    }

    private void fetchDataContainingKeyword(String keyword, long runId) {
        int pageNo = 1;
        while(true){
            VolunteerWorksByKeywordApiResponse apiRepsonse = volunteerWorkApiClient.getVolunteerWorksByKeyword(pageNo, keyword, NUM_OF_ROWS);

            List<VolunteerWorksByKeywordApiResponse.Item> recruitingItems = apiRepsonse.getBody().getItems().stream()
                    .filter(item -> responseUtil.isRecruiting(item))
                    .collect(Collectors.toList());

            // 기존에 있는 엔티티들을 모두 조회
            List<String> registerNumbers = recruitingItems.stream().map(item -> item.getProgrmRegistNo()).collect(Collectors.toList());
            List<VolunteerWork> existingWorks = volunteerWorkRespository.findAllByRegisterNumberIn(registerNumbers);
            Map<String, VolunteerWork> exisitingsByRegisterNumber = existingWorks.stream().collect(Collectors.toMap(work -> work.getRegisterNumber(), work -> work));

            List<VolunteerWork> toInsert = new ArrayList<>();
            recruitingItems.stream().forEach(item ->{
                VolunteerWork work = exisitingsByRegisterNumber.get(item.getProgrmRegistNo());
                if(work != null){
                    // 기존에 있던 데이터면 수정하고
                    work.update(responseUtil.convertItemIntoUpdateRequest(item, runId));
                }
                else{
                    // 없던 데이터면 추가
                    toInsert.add(responseUtil.convertItemIntoEntity(item, runId));
                }
            });

            // 새로운 데이터 저장
            if(!toInsert.isEmpty()){
                volunteerWorkRespository.saveAll(toInsert);
                volunteerWorkRespository.flush();
            }

            if(responseUtil.isLastPage(apiRepsonse)){
                break;
            }
            pageNo++;
        }
    }
}
