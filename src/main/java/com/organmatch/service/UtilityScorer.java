package com.organmatch.service;

import com.organmatch.csp.ScoringFunction;
import com.organmatch.csp.constraints.HlaConstraint;
import com.organmatch.csp.constraints.IschemiaTimeConstraint;
import com.organmatch.model.OrganType;
import com.organmatch.model.OrganUnit;
import com.organmatch.model.Recipient;
import com.organmatch.util.TravelTimeService;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * Service implementing multi-criteria utility scoring for organ-recipient allocations.
 */
@Service
public class UtilityScorer implements ScoringFunction {

    private final ScoreWeights weights;
    private final TravelTimeService travelTimeService;
    private final HlaConstraint hlaConstraint = new HlaConstraint();

    public UtilityScorer(ScoreWeights weights, TravelTimeService travelTimeService) {
        this.weights = weights;
        this.travelTimeService = travelTimeService;
    }

    @Override
    public double score(OrganUnit organUnit, Recipient recipient) {
        if (organUnit == null || recipient == null) {
            return 0.0;
        }
        Map<String, Double> breakdown = getScoreBreakdown(organUnit, recipient);
        return breakdown.getOrDefault("totalScore", 0.0);
    }

    /**
     * Calculates the individual sub-component scores and final weighted score breakdown.
     *
     * @param organUnit Available organ unit
     * @param recipient Candidate recipient
     * @return Map of component names to calculated score values
     */
    public Map<String, Double> getScoreBreakdown(OrganUnit organUnit, Recipient recipient) {
        Map<String, Double> breakdown = new HashMap<>();

        if (organUnit == null || organUnit.getDonor() == null || recipient == null) {
            breakdown.put("urgencyScore", 0.0);
            breakdown.put("waitingDaysScore", 0.0);
            breakdown.put("hlaMatchScore", 0.0);
            breakdown.put("ageBenefitScore", 0.0);
            breakdown.put("transportScore", 0.0);
            breakdown.put("totalScore", 0.0);
            return breakdown;
        }

        // 1. Urgency score: urgency / 10.0
        double urgencyScore = Math.min(Math.max(recipient.getUrgency() / 10.0, 0.0), 1.0);

        // 2. Waiting days score: min(waitingDays, 2500) / 2500.0
        double waitingDaysScore = Math.min(Math.max(recipient.getWaitingDays() / 2500.0, 0.0), 1.0);

        // 3. HLA match score: matches / 6.0 for KIDNEY, 0.5 default for other organs
        double hlaMatchScore;
        if (organUnit.getOrganType() == OrganType.KIDNEY) {
            int matches = hlaConstraint.countHlaMatches(organUnit.getDonor().getHla(), recipient.getHla());
            hlaMatchScore = matches / 6.0;
        } else {
            hlaMatchScore = 0.5;
        }

        // 4. Age benefit score: (1 - recipient.age / 80)
        double ageBenefitScore = Math.min(Math.max(1.0 - (recipient.getAge() / 80.0), 0.0), 1.0);

        // 5. Transport score: (1 - travelHours / maxIschemiaHours)
        double travelHours = travelTimeService.hours(organUnit.getDonor().getCity(), recipient.getCity())
                + IschemiaTimeConstraint.PREPARATION_BUFFER_HOURS;
        double maxIschemia = organUnit.getOrganType().getMaxIschemiaHours();
        double transportScore = Math.min(Math.max(1.0 - (travelHours / maxIschemia), 0.0), 1.0);

        double totalScore = (urgencyScore * weights.getUrgencyWeight())
                + (waitingDaysScore * weights.getWaitingDaysWeight())
                + (hlaMatchScore * weights.getHlaMatchWeight())
                + (ageBenefitScore * weights.getAgeBenefitWeight())
                + (transportScore * weights.getTransportPenaltyWeight());

        totalScore = Math.min(Math.max(totalScore, 0.0), 1.0);

        breakdown.put("urgencyScore", urgencyScore);
        breakdown.put("waitingDaysScore", waitingDaysScore);
        breakdown.put("hlaMatchScore", hlaMatchScore);
        breakdown.put("ageBenefitScore", ageBenefitScore);
        breakdown.put("transportScore", transportScore);
        breakdown.put("totalScore", totalScore);

        return breakdown;
    }
}
