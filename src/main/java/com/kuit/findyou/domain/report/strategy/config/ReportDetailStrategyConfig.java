package com.kuit.findyou.domain.report.strategy.config;

import com.kuit.findyou.domain.report.model.Report;
import com.kuit.findyou.domain.report.model.ReportTag;
import com.kuit.findyou.domain.report.strategy.MissingReportDetailStrategy;
import com.kuit.findyou.domain.report.strategy.ProtectingReportDetailStrategy;
import com.kuit.findyou.domain.report.strategy.ReportDetailStrategy;
import com.kuit.findyou.domain.report.strategy.WitnessReportDetailStrategy;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

@Configuration
@RequiredArgsConstructor
public class ReportDetailStrategyConfig {

    private final ProtectingReportDetailStrategy protectingStrategy;
    private final MissingReportDetailStrategy missingStrategy;
    private final WitnessReportDetailStrategy witnessStrategy;

    @Bean
    public Map<ReportTag, ReportDetailStrategy<? extends Report, ?>> reportDetailStrategies() {
        return Map.of(
                ReportTag.PROTECTING, protectingStrategy,
                ReportTag.MISSING, missingStrategy,
                ReportTag.WITNESS, witnessStrategy
        );
    }
}

