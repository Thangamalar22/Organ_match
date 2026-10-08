package com.organmatch.service;

import com.organmatch.model.OrganType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * Composite DTO encapsulating role-specific metrics for dashboard views.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DashboardDTO {
    private boolean isAdminOrCoordinator;
    private boolean isHospital;
    private boolean isAdmin;

    // Admin & Coordinator Metrics
    private long totalRecipients;
    private long totalDonors;
    private long totalMatchesRun;
    private Map<OrganType, Long> recipientCountByOrgan;
    private List<MatchRunSummaryDTO> recentMatchRuns;
    private double avgCspUtility;
    private double avgGreedyUtility;

    // Admin Only Metrics
    private Map<String, Long> userCountByRole;

    // Hospital Only Metrics
    private String hospitalName;
    private long hospitalRecipientCount;
    private long hospitalDonorCount;
    private List<HospitalRecentEntryDTO> hospitalRecentEntries;
}
