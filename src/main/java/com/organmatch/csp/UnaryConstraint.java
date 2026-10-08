package com.organmatch.csp;

import com.organmatch.model.OrganUnit;
import com.organmatch.model.Recipient;

/**
 * Functional interface for unary constraints evaluating single organ-recipient pairs.
 * Used for node consistency pruning.
 */
@FunctionalInterface
public interface UnaryConstraint {
    /**
     * Checks if recipient r is satisfied for organ unit o.
     *
     * @param organUnit Organ unit
     * @param recipient Candidate recipient
     * @return true if valid, false if constraint violated
     */
    boolean isSatisfied(OrganUnit organUnit, Recipient recipient);
}
