package com.organmatch.service;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO comparing CSP solver allocation against Greedy baseline allocation.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ComparisonResult {
    private MatchResult cspResult;
    private MatchResult greedyResult;
    private double utilityDifference;
    private double utilityPercentageGain;
    private long timeDifferenceMillis;
}
