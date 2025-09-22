package com.kuit.findyou.domain.home.service;

import com.kuit.findyou.domain.home.dto.response.ProtectingAnimalPreview;
import com.kuit.findyou.domain.home.dto.response.WitnessedOrMissingAnimalPreview;

import java.util.List;

public interface RetrieveHomeSectionService {
    List<ProtectingAnimalPreview> retrieveProtectingReportPreviews(Double latitude, Double longitude, int size);

    List<WitnessedOrMissingAnimalPreview> retrieveWitnessedOrMissingReportPreviews(Double latitude, Double longitude, int size);

    List<ProtectingAnimalPreview> retrieveProtectingReportPreviews(int size);

    List<WitnessedOrMissingAnimalPreview> retrieveWitnessedOrMissingReportPreviews(int size);

}
