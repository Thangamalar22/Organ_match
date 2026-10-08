package com.organmatch.csp.constraints;

import com.organmatch.csp.UnaryConstraint;
import com.organmatch.model.OrganUnit;
import com.organmatch.model.Recipient;

/**
 * Interface extending UnaryConstraint with name and human-readable explanation.
 */
public interface NamedUnaryConstraint extends UnaryConstraint {
    /**
     * Returns the name of the constraint.
     */
    String name();

    /**
     * Provides a human-readable explanation of why a recipient candidate was eliminated for an organ unit.
     *
     * @param organUnit Available organ unit
     * @param recipient Candidate recipient
     * @return Human-readable explanation string
     */
    String getExplanation(OrganUnit organUnit, Recipient recipient);
}
