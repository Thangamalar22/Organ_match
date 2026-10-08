package com.organmatch.csp;

import com.organmatch.model.OrganUnit;
import com.organmatch.model.Recipient;

/**
 * Functional interface for scoring the utility of allocating an organ unit to a recipient.
 */
@FunctionalInterface
public interface ScoringFunction {
    /**
     * Calculates utility score for allocating organUnit to recipient.
     *
     * @param organUnit Available organ unit
     * @param recipient Candidate recipient (null for UNASSIGNED)
     * @return Double utility score
     */
    double score(OrganUnit organUnit, Recipient recipient);
}
