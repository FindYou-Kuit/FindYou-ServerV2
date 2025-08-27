package com.kuit.findyou.domain.information.service.volunteerWork;

import com.kuit.findyou.domain.information.repository.VolunteerWorkRepository;
import com.kuit.findyou.domain.information.dto.GetVolunteerWorksResponse;
import com.kuit.findyou.domain.information.model.VolunteerWork;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service
public class VolunteerWorkServiceImpl implements VolunteerWorkService {
    private final VolunteerWorkRepository volunteerWorkRepository;
    @Override
    public GetVolunteerWorksResponse getVolunteerWorksByCursor(Long lastId, int size) {
        log.info("[getVolunteerWorks] lastId = {}", lastId);
        List<VolunteerWork> volunteerWorks = volunteerWorkRepository.findAllByIdLessThanOrderByIdDesc(lastId, PageRequest.of(0, size + 1));
        boolean isLast = volunteerWorks.size() <= size;
        List<VolunteerWork> takenWithSize = takeWithSize(size, volunteerWorks);
        Long newLastId = getNewLastId(takenWithSize);
        return GetVolunteerWorksResponse.from(takenWithSize, newLastId, isLast);
    }

    private static long getNewLastId(List<VolunteerWork> takenWithSize) {
        return takenWithSize.size() > 0 ? takenWithSize.get(takenWithSize.size() - 1).getId() : -1L;
    }

    private static List<VolunteerWork> takeWithSize(int size, List<VolunteerWork> volunteerWorks) {
        return volunteerWorks.size() > size ? volunteerWorks.subList(0, size) : volunteerWorks;
    }
}
