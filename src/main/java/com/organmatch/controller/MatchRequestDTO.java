package com.organmatch.controller;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for matching execution requests.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MatchRequestDTO {

    private String donorId;

    @Builder.Default
    private String algorithm = "CSP"; // "CSP", "GREEDY", or "COMPARE"

    @Builder.Default
    private boolean nodeConsistencyEnabled = true;

    @Builder.Default
    private boolean mrvEnabled = true;

    @Builder.Default
    private boolean degreeHeuristicEnabled = true;

    @Builder.Default
    private boolean lcvEnabled = true;

    @Builder.Default
    private boolean forwardCheckingEnabled = true;

    @Builder.Default
    private boolean allowUnassigned = true;

    @Builder.Default
    private boolean branchAndBoundEnabled = true;
}
