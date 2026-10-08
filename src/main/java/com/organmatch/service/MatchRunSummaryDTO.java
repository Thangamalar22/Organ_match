package com.organmatch.service;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO summarizing a single historical match run.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MatchRunSummaryDTO {
    private Long id;
    private String donorId;
    private String runBy;
    private LocalDateTime runAt;
    private String algorithm;
    private double totalUtility;
    private int organsAssigned;
    private int organsUnassigned;
    private long timeMillis;
}
