package com.organmatch.csp;

import lombok.Data;

/**
 * Tracks execution metrics and statistics for the CSP solver.
 */
@Data
public class SolverStats {
    private long nodesExpanded = 0;
    private long backtracks = 0;
    private long domainsPrunedByNodeConsistency = 0;
    private long forwardCheckingPrunes = 0;
    private long timeMillis = 0;

    public void incrementNodesExpanded() {
        nodesExpanded++;
    }

    public void incrementBacktracks() {
        backtracks++;
    }

    public void addNodeConsistencyPrunes(long count) {
        domainsPrunedByNodeConsistency += count;
    }

    public void incrementForwardCheckingPrunes() {
        forwardCheckingPrunes++;
    }
}
