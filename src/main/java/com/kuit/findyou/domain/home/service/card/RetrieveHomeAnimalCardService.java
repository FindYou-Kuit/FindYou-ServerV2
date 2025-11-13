package com.kuit.findyou.domain.home.service.card;

import com.kuit.findyou.domain.home.dto.response.ProtectingAnimalCard;
import com.kuit.findyou.domain.home.dto.response.WitnessedOrMissingAnimalCard;

import java.util.List;

public interface RetrieveHomeAnimalCardService {
    List<ProtectingAnimalCard> retrieveProtectingReportCards(Double latitude, Double longitude, int size);

    List<WitnessedOrMissingAnimalCard> retrieveWitnessedOrMissingReportCards(Double latitude, Double longitude, int size);

    List<ProtectingAnimalCard> retrieveProtectingReportCards(int size);

    List<WitnessedOrMissingAnimalCard> retrieveWitnessedOrMissingReportCards(int size);

}
