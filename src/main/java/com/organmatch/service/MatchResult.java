package com.organmatch.service;

import com.organmatch.csp.SearchTrace;
import com.organmatch.csp.SolverStats;
import com.organmatch.csp.Variable;
import com.organmatch.model.Donor;
import com.organmatch.model.OrganUnit;
import com.organmatch.model.Recipient;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * DTO encapsulating the outcome of an organ matching allocation run (CSP or Greedy).
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MatchResult {
    private String algorithmName;
    private Donor donor;
    private Map<Variable, Recipient> assignments;
    private List<OrganUnit> unassignedOrgans;
    private double totalUtility;
    private SolverStats stats;
    private SearchTrace trace;
    private Map<OrganUnit, OrganMatchExplanation> perOrganExplanations;
}
