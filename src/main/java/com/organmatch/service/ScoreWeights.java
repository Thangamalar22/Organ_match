package com.organmatch.service;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Component holding weight parameters for multi-criteria utility scoring.
 */
@Component
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ScoreWeights {

    @Value("${app.weights.urgency:0.40}")
    @Builder.Default
    private double urgencyWeight = 0.40;

    @Value("${app.weights.waiting-days:0.20}")
    @Builder.Default
    private double waitingDaysWeight = 0.20;

    @Value("${app.weights.hla-match:0.20}")
    @Builder.Default
    private double hlaMatchWeight = 0.20;

    @Value("${app.weights.age-benefit:0.10}")
    @Builder.Default
    private double ageBenefitWeight = 0.10;

    @Value("${app.weights.transport-penalty:0.10}")
    @Builder.Default
    private double transportPenaltyWeight = 0.10;
}
