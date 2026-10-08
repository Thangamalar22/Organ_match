package com.organmatch.service;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO holding aggregated metrics for a single CSP solver configuration in automated experiments.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ExperimentConfigResult {
    private String configName;
    private double avgNodesExpanded;
    private double avgBacktracks;
    private double avgTimeMillis;
    private double avgTotalUtility;
    private double avgOrgansAssigned;
}
