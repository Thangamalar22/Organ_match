package com.organmatch.service;

import com.organmatch.model.OrganUnit;
import com.organmatch.model.Recipient;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * DTO detailing the allocation explanation, score breakdown, and top rejected candidates for a single organ unit.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OrganMatchExplanation {
    private OrganUnit organUnit;
    private Recipient chosenRecipient; // null if unassigned
    private double utilityScore;
    private Map<String, Double> scoreBreakdown;
    private List<CandidateElimination> topRejectedCandidates;
}
