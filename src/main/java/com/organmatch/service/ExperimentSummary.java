package com.organmatch.service;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO holding the complete summary of automated multi-configuration experiments.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ExperimentSummary {
    private int donorCount;
    private int recipientCount;
    private long seed;
    private List<ExperimentConfigResult> configResults;
    private double cspAvgUtility;
    private double greedyAvgUtility;
    private double cspAvgAssignedOrgans;
    private double greedyAvgAssignedOrgans;
    private double utilityGainPercentage;
    private long totalRunTimeMillis;
}
