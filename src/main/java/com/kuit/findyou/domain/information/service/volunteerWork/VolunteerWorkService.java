package com.kuit.findyou.domain.information.service.volunteerWork;

import com.kuit.findyou.domain.information.dto.response.GetVolunteerWorksResponse;

public interface VolunteerWorkService {
    GetVolunteerWorksResponse getVolunteerWorksByCursor(Long lastId, int size);
}
