package com.organmatch.csp;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Configuration settings to toggle CSP engine features for comparison and experimentation.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SolverConfig {
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

    @Builder.Default
    private double unassignedPenalty = 0.0;

    @Builder.Default
    private long maxNodesExpanded = 100000; // Safety node expansion limit to prevent hanging
}
