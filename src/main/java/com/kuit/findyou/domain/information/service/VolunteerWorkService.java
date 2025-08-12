package com.kuit.findyou.domain.information.service;

import com.kuit.findyou.domain.information.dto.GetVolunteerWorksResponse;

public interface VolunteerWorkService {
    GetVolunteerWorksResponse getVolunteerWorksByCursor(Long lastId, int size);
}
