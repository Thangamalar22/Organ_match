package com.organmatch.csp.constraints;

import com.organmatch.csp.GlobalConstraint;
import com.organmatch.csp.Variable;
import com.organmatch.model.Recipient;

import java.util.Map;

/**
 * Interface extending GlobalConstraint with name and human-readable explanation.
 */
public interface NamedGlobalConstraint extends GlobalConstraint {
    /**
     * Returns the name of the global constraint.
     */
    String name();

    /**
     * Provides a human-readable explanation of why a candidate assignment is inconsistent.
     *
     * @param partialAssignment Current partial assignment map
     * @param variable Variable being assigned
     * @param recipient Candidate recipient
     * @return Human-readable explanation string
     */
    String getExplanation(Map<Variable, Recipient> partialAssignment, Variable variable, Recipient recipient);
}
