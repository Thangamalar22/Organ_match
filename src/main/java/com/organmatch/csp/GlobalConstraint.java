package com.organmatch.csp;

import com.organmatch.model.Recipient;

import java.util.Map;

/**
 * Functional interface for global / binary constraints evaluating consistency across assignments.
 */
@FunctionalInterface
public interface GlobalConstraint {
    /**
     * Checks if assigning recipient r to variable v is consistent with the current partial assignment.
     *
     * @param partialAssignment Current assignments of variables to recipients
     * @param variable Variable being assigned
     * @param recipient Candidate recipient for variable (can be null for UNASSIGNED)
     * @return true if consistent, false if constraint violated
     */
    boolean isConsistent(Map<Variable, Recipient> partialAssignment, Variable variable, Recipient recipient);
}
