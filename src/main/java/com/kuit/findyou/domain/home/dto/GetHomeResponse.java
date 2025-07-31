package com.kuit.findyou.domain.home.dto;

import java.util.List;

public record GetHomeResponse(
        TotalStatistics statistics,
        List<ProtectingAnimalPreview> protectingAnimals,
        List<WitnessedOrMissingAnimalPreview> witnessedOrMissingAnimals
) {
    public record TotalStatistics(
            Statistics recent7days,
            Statistics recent3months,
            Statistics recent1Year
    ){ }
    public record Statistics(
            String rescuedAnimalCount,
            String protectingAnimalCount,
            String adoptedAnimalCount,
            String reportedAnimalCount
    ){}
}
